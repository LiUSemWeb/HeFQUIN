package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

/**
 * A generic implementation of a batch-based bind-join algorithm that issues
 * the bind-join requests without blocking, handling the processing of their
 * responses in parallel (in the threads that the federation access manager
 * uses to perform the requests).
 *
 * The implementation is generic in the sense that it works with any form of
 * bind-join requests. Each concrete implementation that extends this base
 * class needs to implement the {@link #createRequest(Set)} method to create
 * the requests that are specific to that concrete implementation.
 *
 * The algorithm works as follows: For every sequence of solution mappings
 * from the input, the algorithm splits this sequence into batches where
 * each such batch will then be used for a separate bind-join request. Each
 * such batch is associated with a sub-multiset of the input solution mappings
 * that are covered by the batch, whereas the batch itself consists of versions
 * of these input solution mappings that are already restricted to the join
 * variables (and that contain no blank nodes, see below). Hence, while the
 * number of such already-restricted solution mappings per batch is fixed
 * (see the {@code batchSize} argument of the constructor), the size of the
 * sub-multiset of input solution mappings associated with each batch may be
 * greater than the batch size.
 * After splitting the current sequence of input solution mappings into
 * batches, the last batch may not be full, in which case it is kept and
 * will be populated further once the next sequence of input solution mappings
 * is passed to the operator. The full batches are used to create bind-join
 * requests, one per batch. The response to such a request is the subset of
 * the solutions for the query/pattern of this operator that are join partners
 * for at least one of the solutions that were used for creating the request. 
 * Each of the requests is issued using the asynchronous functionality of the
 * federation access manager, which results in a {@link CompletableFuture}.
 * The algorithm connects this future to a {@link MyResponseProcessor} to
 * process the response once it arrives (joining the solution mappings from
 * the response with the solution mappings covered by the corresponding batch).
 * All these futures are collected such that the algorithm can wait for their
 * completion after the child operator has stopped producing input for this
 * operator.
 *
 * This implementation is capable of separating out each input solution mapping
 * that assigns a blank node to any of the join variables. Then, such solution
 * mappings are not even considered when creating the requests because they
 * cannot have any join partners in the results obtained from the federation
 * member. Of course, in case the algorithm is used with outer-join semantics,
 * these solution mappings are still returned to the output (without joining
 * them with anything).
 *
 * Another feature of this implementation is that it switches into a
 * full-retrieval mode as soon as there is an input solution mapping that
 * does not have a binding for any of the join variables (which may happen
 * only in cases in which none of the join variables is a certain variable).
 * Such an input solution mapping is compatible with (and, thus, can be joined
 * with) every solution mapping that the federation member has for the query /
 * pattern of this bind-join operator. Therefore, when switching into
 * full-retrieval mode, this implementation performs a request to retrieve
 * the complete set of all these solution mappings and, then, uses this set to
 * find join partners for the current and the future batches of input solution
 * mappings (because, with the complete set available locally, there is no need
 * anymore to issue further bind-join requests). This capability relies on the
 * {@link #createExecutableReqOpForAll()} method that needs to be provided by
 * each concrete implementation that extends this base class.
 */
public abstract class BaseForExecOpParallelBindJoin<
                                       QueryType extends Query,
                                       MemberType extends FederationMember,
                                       ReqType extends DataRetrievalRequest,
                                       RespType extends DataRetrievalResponse<?>>
           extends UnaryExecutableOpBase
{
	public final static int DEFAULT_BATCH_SIZE = 30;

	protected final QueryType query;
	protected final MemberType fm;

	protected final boolean useOuterJoinSemantics;

	protected final Set<Var> varsInQuery;
	protected final boolean allJoinVarsAreCertain;

	/**
	 * The number of solution mappings that this operator
	 * uses for each of the bind join requests.
	 */
	protected final int batchSize;

	/**
	 * This list is used to collect up the input solution mappings (obtained
	 * from the child operator in the execution plan) that are covered by the
	 * currently-assembled batch of solution mappings, where that batch is
	 * in {@link #currentBatch} and will be used for a bind-join request.
	 *
	 * Note that the solution mappings in this collection and the ones in
	 * {@link #currentBatch} are not necessarily the same; the ones in
	 * {@link #currentBatch} are versions of the ones in this collection,
	 * restricted to join variables.
	 * 
	 * Once {@link #currentBatch} is full (i.e., the number of solution
	 * mappings in it is equal to {@link #batchSize}), this list is appended
	 * to {@link #solMapsCoveredPerBatch} and then set back to {@code null},
	 * to be initialized to a new list when another input solution mapping
	 * arrives.
	 */
	protected List<SolutionMapping> solMapsCoveredByCurrentBatch = null;

	protected final List<List<SolutionMapping>> solMapsCoveredPerBatch = new ArrayList<>();

	/**
	 * This set is used to collect up the solution mappings for the currently-
	 * assembled batch, which will then be used for a bind-join request. These
	 * solution mappings will be created by restricting relevant input solution
	 * mappings (obtained from the child operator in the execution plan) to the
	 * join variables; i.e., projecting away the non-join variables, as the
	 * bindings for non-join variables do not need to be shipped in the bind-
	 * join requests.
	 *
	 * The corresponding input solution mappings from which the solution
	 * mappings in this set have been created are collected in parallel in
	 * {@link #solMapsCoveredByCurrentBatch}. It is possible that multiple
	 * input solution mappings may result in the same restricted solution
	 * mapping.
	 *
	 * Once the number of solution mappings in this set is equal to
	 * {@link #batchSize}) (i.e., the current batch is complete), the set
	 * is appended to {@link #batches} and {@link #currentBatch} is set
	 * back to {@code null}, to be initialized with a new set when another
	 * input solution mapping arrives.
	 */
	protected Set<Binding> currentBatch = null;

	protected final List<Set<Binding>> batches = new ArrayList<>();

	/**
	 * Used to collect the completable futures created for processing the
	 * batch requests and their responses.
	 */
	protected final List<CompletableFuture<?>> futures = new ArrayList<>();

	/**
	 * In case that this operator had to switch to full-retrieval mode,
	 * this one contains all solution mappings retrieved for the query
	 * of this operator.
	 */
	// TODO: Use a more suitable data structure for this (some type of hash index).
	protected Iterable<SolutionMapping> fullResult = null;

	// statistics
	private AtomicLong numberOfOutputMappingsProduced = new AtomicLong(0L);
	protected int numberOfRequestsUsed = 0;
	protected List<Long> requestDurationsInMS = new Vector<>(); // Vector is thread safe
	protected List<Integer> numOfSolMapsRetrievedPerReq = new Vector<>();
	protected ExecutableOperatorStats statsOfFullRetrievalReqOp = null;

	/**
	 * @param query - the graph pattern (or other kind of query) to be
	 *          evaluated (in a bind-join manner) at the federation member
	 *          given as 'fm'
	 *
	 * @param varsInQuery - the variables that occur in the 'query'
	 *
	 * @param fm - the federation member targeted by this operator
	 *
	 * @param inputVars - the variables to be expected in the solution
	 *          mappings that will be pushed as input to this operator
	 *
	 * @param useOuterJoinSemantics - <code>true</code> if the 'query' is to
	 *          be evaluated under outer-join semantics; <code>false</code>
	 *          for inner-join semantics
	 *
	 * @param batchSize - the number of solution mappings to be included in
	 *          each bind-join request; this value must not be smaller than
	 *          {@link #minimumRequestBlockSize}
	 *
	 * @param collectExceptions - <code>true</code> if this operator has to
	 *          collect exceptions (which is handled entirely by one of the
	 *          super classes); <code>false</code> if the operator should
	 *          immediately throw every {@link ExecOpExecutionException}
	 */
	public BaseForExecOpParallelBindJoin(
			final QueryType query,
			final Set<Var> varsInQuery,
			final MemberType fm,
			final ExpectedVariables inputVars,
			final boolean useOuterJoinSemantics,
			final int batchSize,
			final boolean collectExceptions,
			final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert query != null;
		assert fm != null;
		assert varsInQuery != null;
		assert batchSize > 0;

		this.query = query;
		this.varsInQuery = varsInQuery;
		this.fm = fm;
		this.useOuterJoinSemantics = useOuterJoinSemantics;
		this.batchSize = batchSize;

		this.allJoinVarsAreCertain = BaseForExecOpSequentialBindJoin.areAllJoinVarsAreCertain(varsInQuery, inputVars);
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		_process( List.of(inputSolMap), sink, execCxt );
	}

	@Override
	protected void _process( final List<SolutionMapping> inputSolMaps,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// Iterate over the given input solution mappings to split them into
		// batches, and to already handle those input solution mappings that
		// do not go into a batch (which would be the ones that have a blank
		// node for one of the join variables and, thus, cannot have join
		// partners). Additionally, if one of the input solution mappings
		// does not contain any of the join variables (and, thus, can be
		// join with every solution for the query of this operator), switch
		// into full-retrieval mode.
		for ( final SolutionMapping sm : inputSolMaps ) {
			batchUp(sm, sink, execCxt);
		}

		// If the resulting collection of *full* batches is not empty
		// (i.e., we have at least one full batch), issue the bind-join
		// requests for all these full batches and initiate the processing
		// of their responses.
		// Notice that we ignore the currently-populated batch (if any) at
		// this point because that batch is not yet full; we will continue
		// populating that batch at the next call of this function or, at
		// the latest, within the '_concludeExecution' function.
		if ( ! batches.isEmpty() ) {
			initiateProcessingOfBatches(sink, execCxt);

			// After performing the requests (and handling their
			// responses), we can forget about the full batches
			// considered for the requests.
			batches.clear();
			solMapsCoveredPerBatch.clear();
		}
	}

	protected void batchUp( final SolutionMapping inputSolMap,
	                        final IntermediateResultElementSink sink,
	                        final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// First, check whether we had to switch into full-retrieval mode,
		// in which case we can find the join partners for the given input
		// solution mapping within the full result that we had to retrieve.
		if ( fullResult != null ) {
			joinInFullRetrievalMode(inputSolMap, sink);
			return;
		}

		// At this point, we know that we are in the normal bind-join mode.
		// Let's first create a version of the input solution mapping that
		// is restricted to the join variables; i.e., we project away all
		// bindings for variables that are not join variables.
		final Binding restrictedInputSolMap = SolutionMappingUtils.restrict(
				inputSolMap.asJenaBinding(), varsInQuery );

		// If the input solution mapping does not contain any of the join
		// variables (and, thus, the restricted version of it is the empty
		// mapping), then it is compatible (and, thus, can be joined) with
		// every solution mapping that the federation member has for the
		// query/pattern of this bind-join operator. In this case, we need
		// to switch into full-retrieval mode.
		if ( restrictedInputSolMap.isEmpty() ) {
			switchToFullRetrievalMode(execCxt, sink);
			joinInFullRetrievalMode(inputSolMap, sink);
			return;
		}

		// If the input solution mapping assigns any of the join variables
		// to a blank node, it cannot have join partners in the federation
		// member of this operator (because blank nodes that the federation
		// member may have are disjoint from blank nodes that we may have
		// obtained from somewhere else). In this case, we do not need to
		// proceed with this input solution mapping; but we have to send it
		// to the output if we are operating under outer-join semantics.
		if ( SolutionMappingUtils.containsBlankNodes(restrictedInputSolMap) ) {
			if ( useOuterJoinSemantics ) {
				numberOfOutputMappingsProduced.incrementAndGet();
				sink.send(inputSolMap);
			}

			return;
		}

		// At this point, we know that we may retrieve join partners for
		// the input solution mapping. Therefore, we add it to the current
		// batch, which may have to be set up first.
		if ( solMapsCoveredByCurrentBatch == null ) {
			// set up the next batch
			solMapsCoveredByCurrentBatch = new ArrayList<>();
			currentBatch = new HashSet<>();
		}

		solMapsCoveredByCurrentBatch.add(inputSolMap);

		// Check whether the restricted version of the given input solution
		// mapping is already covered by a solution mapping of the batch.
		if ( ! alreadyCovered(restrictedInputSolMap) ) {
			// If it is not covered, we need to add it to the batch, but first
			// we may have to remove solution mappings from that set.
			if ( ! allJoinVarsAreCertain ) {
				// Update the current batch by removing the solution mappings
				// that include the given restricted solution mapping.
				// This is okay because the potential join partners captured
				// by these solution mappings are also captured by the given
				// restricted solution mapping, which we will add to the set
				// in the next step. In fact, it is even necessary to do so
				// in order to avoid spurious duplicates in the join result.
				currentBatch.removeIf( sm -> SolutionMappingUtils.includedIn(restrictedInputSolMap, sm) );
			}

			// Now we add it to the batch.
			currentBatch.add(restrictedInputSolMap);

			// Let's check whether the batch is full now.
			if ( currentBatch.size() == batchSize ) {
				// If the current batch is indeed full, we need to add it to
				// the collection of batches to be used for the next round
				// of requests, ...
				batches.add(currentBatch);
				solMapsCoveredPerBatch.add(solMapsCoveredByCurrentBatch);
				// ... and make sure that we will start a new batch for the
				// next input solution mapping (if any).
				currentBatch                 = null;
				solMapsCoveredByCurrentBatch = null;
			}
		}
	}

	protected boolean alreadyCovered( final Binding inputSolMapRestricted ) {
		if ( currentBatch == null )
			return false;

		if ( currentBatch.contains(inputSolMapRestricted) )
			return true;

		if ( ! allJoinVarsAreCertain ) {
			for ( final Binding sm : currentBatch ) {
				if ( SolutionMappingUtils.includedIn(sm, inputSolMapRestricted) ) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		// If we did not have to switch into full-retrieval mode (i.e., we
		// are still in the normal bind-join mode), we need to check whether
		// the last batch of input solution mappings is still incomplete. If
		// it is, it needs to be processed now, even if incomplete.
		if ( fullResult == null ) {
			if ( currentBatch != null && ! currentBatch.isEmpty() ) {
				batches.add(currentBatch);
				solMapsCoveredPerBatch.add(solMapsCoveredByCurrentBatch);

				initiateProcessingOfBatches(sink, execCxt);

				currentBatch                 = null;
				solMapsCoveredByCurrentBatch = null;
			}
		}

		// Now we wait for all the futures to be completed. Note that this
		// needs to be done outside of the previous if-block because, even
		// if we had to switch into full-retrieval mode, we may have futures
		// from before we made that switch.
		final CompletableFuture<?>[] arr = futures.toArray( new CompletableFuture<?>[0] );
		try {
			CompletableFuture.allOf(arr).get();
		}
		catch ( final InterruptedException e ) {
			throw new ExecOpExecutionException("Interruption of the futures that perform the requests and process the responses", e, this);
		}
		catch ( final ExecutionException e ) {
			throw new ExecOpExecutionException("The execution of the futures that perform the requests and process the responses caused an exception.", e, this);
		}
	}

	/**
	 * Issues a bind-join request for every *full* batch of solution mappings
	 * that has been created from the given input solution mappings up to this
	 * point. The {@link CompletableFuture}s obtained for issuing each of these
	 * requests is connected to a {@link MyResponseProcessor} that will handle
	 * the processing of the response (joining the solution mappings from the
	 * response with the solution mappings covered by the corresponding batch.
	 */
	protected void initiateProcessingOfBatches( final IntermediateResultElementSink sink,
	                                            final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		assert batches.size() == solMapsCoveredPerBatch.size();

		// Iterate over the *full* batches, including the corresponding
		// collections of solution mappings covered by these batches.
		final Iterator<Set<Binding>> itBatches = batches.iterator();
		final Iterator<List<SolutionMapping>> it2 = solMapsCoveredPerBatch.iterator();
		while ( itBatches.hasNext() ) {
			final Set<Binding> batch = itBatches.next();
			final List<SolutionMapping> solMapsCoveredByBatch = it2.next();

			// Create the bind-join request for the current batch and, once
			// this is done, we can already forget the solution mappings of
			// that batch (to enable the GC to free up memory early).
			final ReqType req = createRequest(batch);
			batch.clear();

			// Issue the request via the federation access manager.
			final CompletableFuture<RespType> f;
			try {
				f = execCxt.getFederationAccessMgr().issueRequest(req, fm);
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
			}

			numberOfRequestsUsed++;

			// Create a response processor that shall handle the response
			// obtained via the bind-join request (namely, joining it with
			// the solution mappings covered by the current batch).
			final MyResponseProcessor respProc = new MyResponseProcessor( solMapsCoveredByBatch,
			                                                              sink );

			// Attach the response processor to the future for the request and
			// remember the future so thatwe can wait for its completion later.
			futures.add( f.thenAccept(respProc) );
		}
	}

	/**
	 * Such a response processor will obtain the result from a bind-join
	 * request and join that result with the solution mappings that have
	 * been covered by the batch used for creating the request.
	 */
	protected class MyResponseProcessor implements Consumer<RespType> {
		protected final List<SolutionMapping> solMapsCoveredByBatch;
		protected final IntermediateResultElementSink sink;

		public MyResponseProcessor( final List<SolutionMapping> solMapsCoveredByBatch,
		                            final IntermediateResultElementSink sink ) {
			this.solMapsCoveredByBatch = solMapsCoveredByBatch;
			this.sink = sink;
		}

		@Override
		public void accept( final RespType response ) {
			final Iterable<SolutionMapping> solmaps;
			try {
				solmaps = extractSolMaps(response);
			}
			catch( final UnsupportedOperationDueToRetrievalError e ) {
				recordException( "Accessing the response caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e );
				return;
			}

			final long outputCount;
			if ( useOuterJoinSemantics )
				outputCount = leftOuterJoin(solMapsCoveredByBatch, solmaps, sink);
			else
				outputCount = innerJoin(solMapsCoveredByBatch, solmaps, sink);

			// Update statistics.
			numberOfOutputMappingsProduced.addAndGet(outputCount);
			requestDurationsInMS.add( response.getRequestDuration().toMillis() );
			if ( solmaps instanceof Collection c ) {
				numOfSolMapsRetrievedPerReq.add( c.size() );
			}

			// We are done here and can forget the solution mappings
			// from which the processed batch was created.
			solMapsCoveredByBatch.clear();
		}
	}

	protected void recordException( final String msg,
	                                final Exception cause ) {
		final ExecOpExecutionException e = new ExecOpExecutionException(msg, cause, this);
		recordExceptionCaughtDuringExecution(e);
	}

	protected long innerJoin( final Iterable<SolutionMapping> left,
	                          final Iterable<SolutionMapping> right,
	                          final IntermediateResultElementSink sink ) {
		long outputCount = 0L;
		for ( final SolutionMapping sm1 : left ) {
			for ( final SolutionMapping sm2 : right ) {
				if ( SolutionMappingUtils.compatible(sm1, sm2) ) {
					sink.send( SolutionMappingUtils.merge(sm1,sm2) );
					outputCount++;
				}
			}
		}

		return outputCount;
	}

	protected long leftOuterJoin( final Iterable<SolutionMapping> left,
	                              final Iterable<SolutionMapping> right,
	                              final IntermediateResultElementSink sink ) {
		long outputCount = 0L;
		for ( final SolutionMapping sm1 : left ) {
			boolean hasJoinPartner = false;
			for ( final SolutionMapping sm2 : right ) {
				if ( SolutionMappingUtils.compatible(sm1, sm2) ) {
					sink.send( SolutionMappingUtils.merge(sm1,sm2) );
					outputCount++;
					hasJoinPartner = true;
				}
			}

			if ( ! hasJoinPartner ) {
				sink.send(sm1);
				outputCount++;
			}
		}

		return outputCount;
	}

	/**
	 * Implementations of this function should create a bind-join request
	 * for the given batch of solution mappings.
	 *
	 * Implementations can assume that the given solution mappings are already
	 * restricted to contain bindings only for the join variables, that none
	 * of the given solution mappings contains blank nodes, that none of the
	 * given solution mappings is the empty solution mapping, and that the
	 * given set of solution mappings is duplicate free and nonempty.
	 */
	protected abstract ReqType createRequest( Set<Binding> batch );

	/**
	 * Implementations of this function should extract solution mappings
	 * from the given response (obtained via a bind-join request).
	 *
	 * @throws UnsupportedOperationDueToRetrievalError if the given response
	 *                      does not contain retrieved data but an indication
	 *                      of a data retrieval error
	 */
	protected abstract Iterable<SolutionMapping> extractSolMaps( RespType response ) throws UnsupportedOperationDueToRetrievalError;


	// ------- functionality for full-retrieval mode ------

	/**
	 * Performs a request to retrieve all solution mappings for the query
	 * of this operator (see {@link #createExecutableReqOpForAll}), puts
	 * the retrieved solution mappings into {@link #fullResult}, and joins
	 * these retrieved solution mappings with all the input solution mappings
	 * collected so far; resulting solution mappings are send to the given sink.
	 */
	protected void switchToFullRetrievalMode( final ExecutionContext execCxt,
	                                          final IntermediateResultElementSink sink )
			throws ExecOpExecutionException
	{
		obtainFullResult(execCxt);
		handleCollectedSolMaps(sink);
	}

	/**
	 * Performs a request to retrieve all solution mappings for the query
	 * of this operator (see {@link #createExecutableReqOpForAll}) and puts
	 * the retrieved solution mappings into {@link #fullResult}.
	 */
	protected void obtainFullResult( final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableReqOpForAll();

		final CollectingIntermediateResultElementSink mySink = new CollectingIntermediateResultElementSink();
		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
			throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
		}

		statsOfFullRetrievalReqOp = reqOp.getStats();

		fullResult = mySink.getCollectedSolutionMappings();
	}

	/**
	 * Makes sure that all the input solution mappings collected so far are
	 * joined with the retrieved solution mappings in {@link #fullResult};
	 * resulting solution mappings are send to the given sink.
	 */
	protected void handleCollectedSolMaps( final IntermediateResultElementSink sink ) {
		for ( final List<SolutionMapping> b : solMapsCoveredPerBatch ) {
			joinInFullRetrievalMode(b, sink);
		}

		solMapsCoveredPerBatch.clear();
		batches.clear();

		if ( solMapsCoveredByCurrentBatch != null ) {
			joinInFullRetrievalMode(solMapsCoveredByCurrentBatch, sink);

			solMapsCoveredByCurrentBatch.clear();
			solMapsCoveredByCurrentBatch = null;

			currentBatch.clear();
			currentBatch = null;
		}
	}

	protected void joinInFullRetrievalMode( final Iterable<SolutionMapping> batchOfSolMaps,
	                                        final IntermediateResultElementSink sink )
	{
		for ( final SolutionMapping inputSM : batchOfSolMaps ) {
			joinInFullRetrievalMode(inputSM, sink);
		}
	}

	protected void joinInFullRetrievalMode( final SolutionMapping inputSolMap,
	                                        final IntermediateResultElementSink sink )
	{
		long cnt = 0L;
		boolean hasJoinPartners = false;
		for ( final SolutionMapping retrievedSM : fullResult ) {
			if ( SolutionMappingUtils.compatible(retrievedSM, inputSolMap) ) {
				hasJoinPartners = true;
				cnt++;
				sink.send( SolutionMappingUtils.merge(retrievedSM, inputSolMap) );
			}
		}

		if ( useOuterJoinSemantics && ! hasJoinPartners ) {
			cnt++;
			sink.send(inputSolMap);
		}

		numberOfOutputMappingsProduced.addAndGet(cnt);
	}

	/**
	 * Implementations of this function should create an executable operator
	 * that can perform a request to retrieve all solution mappings for the
	 * query of this operator (i.e., not a bind-join request).
	 *
	 * The operator created by this function should throws exceptions instead
	 * of collecting them.
	 */
	protected abstract NullaryExecutableOp createExecutableReqOpForAll();


	// ------- functionality for Stats ------

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced.set(0L);
		numberOfRequestsUsed = 0;
		requestDurationsInMS.clear();
		numOfSolMapsRetrievedPerReq.clear();
		statsOfFullRetrievalReqOp = null;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "queryAsString",      query.toString() );
		s.put( "fedMemberAsString",  fm.toString() );
		s.put( "numberOfOutputMappingsProduced",   Long.valueOf(numberOfOutputMappingsProduced.get()) );
		s.put( "hadToSwitchToFullRetrievalMode",   Boolean.valueOf(fullResult != null) );
		s.put( "numberOfRequestsUsed",             Integer.valueOf(numberOfRequestsUsed) );
		s.put( "requestDurationsInMS",             requestDurationsInMS.toString() );
		s.put( "numberOfSolMapsRetrievedPerReqOp", numOfSolMapsRetrievedPerReq.toString() );

		if ( statsOfFullRetrievalReqOp != null) {
			s.put( "statsOfFullRetrievalReqOp",  statsOfFullRetrievalReqOp );
		}

		return s;
	}

}
