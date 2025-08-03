package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

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

/**
 * A generic implementation of (a batching version of) the bind join algorithm
 * that uses executable request operators for performing the requests to the
 * federation member.
 *
 * The implementation is generic in the sense that it works with any type of
 * request operator. Each concrete implementation that extends this base class
 * needs to implement the {@link #createExecutableReqOp(Set)} method to create
 * the request operators with the types of requests that are specific to that
 * concrete implementation.
 *
 * The algorithm collects solution mappings from the input. Once enough
 * solution mappings have arrived, the algorithm creates the corresponding
 * request (see above) and sends this request to the federation member (the
 * algorithm may even decide to split the input batch into smaller batches
 * for multiple requests; see below). The response to such a request is the
 * subset of the solutions for the query/pattern of this operator that are
 * join partners for at least one of the solutions that were used for creating
 * the request. After receiving such a response, the algorithm locally joins
 * the solutions from the response with the solutions in the batch used for
 * creating the request, and outputs the resulting joined solutions (if any).
 * Thereafter, the algorithm moves on to collect the next solution mappings
 * from the input, until it can do the next request, etc.
 *
 * This implementation is capable of separating out each input solution mapping
 * that assigns a blank node to any of the join variables. Then, such solution
 * mappings are not even considered when creating the requests because they
 * cannot have any join partners in the results obtained from the federation
 * member. Of course, in case the algorithm is used with outer-join semantics,
 * these solution mappings are still returned to the output (without joining
 * them with anything).
 *
 * A feature of this implementation is that, in case a request operator fails,
 * this implementation automatically reduces the batch size for requests and,
 * then, tries to re-process (with the reduced request batch size) the input
 * solution mappings for which the request operator failed.
 *
 * Another feature of this implementation is that it can switch into a
 * full-retrieval mode as soon as there is an input solution mapping that
 * does not have a binding for any of the join variables (which may happen
 * only in cases in which at least one of the join variables is a certain
 * variable). Such an input solution mapping is compatible with (and, thus,
 * can be joined with) every solution mapping that the federation member has 
 * for the query/pattern of this bind-join operator. Therefore, when switching
 * into full-retrieval mode, this implementation performs a request to retrieve
 * the complete set of all these solution mappings and, then, uses this set to
 * find join partners for the current and the future batches of input solution
 * mappings (because, with the complete set available locally, there is no need
 * anymore to issue further bind-join requests). This capability relies on the
 * {@link #createExecutableReqOpForAll()} method that needs to be provided by
 * each concrete implementation that extends this base class.
 */
public abstract class BaseForExecOpBindJoinWithRequestOps<QueryType extends Query,
                                                          MemberType extends FederationMember>
           extends UnaryExecutableOpBase
{
	public final static int DEFAULT_BATCH_SIZE = 30;

	protected final QueryType query;
	protected final MemberType fm;

	protected final Set<Var> varsInQuery;
	protected final boolean useOuterJoinSemantics;

	protected final boolean allJoinVarsAreCertain;

	/**
	 * The number of solution mappings that this operator uses for each
	 * of the bind join requests. This number may be adapted at runtime.
	 */
	protected int requestBlockSize;

	/**
	 * The minimum value to which {@link #requestBlockSize} can be reduced.
	 */
	protected static final int minimumRequestBlockSize = 5;

	/**
	 * This set is used to collect up the input solution mappings (obtained
	 * from the child operator in the execution plan) for which the next
	 * bind-join request will ask for possible join partners.
	 *
	 * Note that these are not necessarily the solution mappings to be used
	 * for forming the next bind-join request; those are collected in parallel
	 * in {@link #currentSolMapsForRequest}.
	 * 
	 * Once the response received for the next bind-join request has been
	 * handled, this set will be cleared (and then populated again, by using
	 * the next input solution mappings that will arrive afterwards).
	 */
	protected final Set<SolutionMapping> currentBatch = new HashSet<>();

	/**
	 * This set is used to collect up solution mappings that will be used
	 * to form the next bind-join request. These solution mappings will be
	 * created by restricting relevant input solution mappings (obtained
	 * from the child operator in the execution plan) to the join variables;
	 * i.e., projecting away the non-join variables, as the bindings for these
	 * do not need to be shipped in the bind-join requests.
	 *
	 * The corresponding input solution mappings from which the solution
	 * mappings in this set have been created are collected in parallel in
	 * {@link #currentBatch}. It is possible that multiple input solution
	 * mappings may result in the same restricted solution mapping.
	 * 
	 * Once the response received for the next bind-join request has been
	 * handled, this set will be cleared (and then populated again, by using
	 * the next input solution mappings that will arrive afterwards).
	 */
	protected final Set<Binding> currentSolMapsForRequest = new HashSet<>();

	/**
	 * In case that this operator had to switch to full-retrieval mode,
	 * this one contains all solution mappings retrieved for the query
	 * of this operator.
	 */
	// TODO: Use a more suitable data structure for this (some type of hash index).
	protected Iterable<SolutionMapping> fullResult = null;

	// statistics
	private long numberOfOutputMappingsProduced = 0L;
	protected boolean requestBlockSizeWasReduced = false;
	protected int numberOfRequestOpsUsed = 0;
	protected ExecutableOperatorStats statsOfFirstReqOp = null;
	protected ExecutableOperatorStats statsOfLastReqOp = null;

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
	public BaseForExecOpBindJoinWithRequestOps( final QueryType query,
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
		assert batchSize >= minimumRequestBlockSize;

		this.query = query;
		this.varsInQuery = varsInQuery;
		this.fm = fm;
		this.useOuterJoinSemantics = useOuterJoinSemantics;
		this.requestBlockSize = batchSize;

		this.allJoinVarsAreCertain = areAllJoinVarsAreCertain(varsInQuery, inputVars);
	}

	/**
	 * Returns <code>true</code> if the given set of variables does not overlap
	 * with the possible-variables set of the given {@link ExpectedVariables}.
	 */
	public static boolean areAllJoinVarsAreCertain( final Set<Var> varsInQuery,
	                                                final ExpectedVariables inputVars ) {
		// The join variables are all certain variables if there is
		// no overlap between the input variables that are possible
		// variables sand the variables in the given query.
		final Iterator<Var> it = inputVars.getPossibleVariables().iterator();
		while ( it.hasNext() ) {
			final Var possibleVar = it.next();
			if ( varsInQuery.contains(possibleVar) )
				// Found a variable that is both a possible input
				// variable and in the given query. Hence, this
				// would be a join variable that is not certain.
				return false;
		}

		return true;
	}

	@Override
	protected void _process( final SolutionMapping inputSolMap,
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
		final Binding restrictedInputSolMap = SolutionMappingUtils.restrict( inputSolMap.asJenaBinding(),
		                                                                     varsInQuery );

		// If the input solution mapping does not contain any of the join
		// variables (and, thus, the restricted version of it is the empty
		// mapping), then it is compatible (and, thus, can be joined) with
		// every solution mapping that the federation member has for the
		// query/pattern of this bind-join operator. In this case, we need
		// to switch into full-retrieval mode.
		if ( restrictedInputSolMap.isEmpty() ) {
			switchToFullRetrievalMode(execCxt);

			joinInFullRetrievalMode(inputSolMap, sink);
			joinInFullRetrievalMode(currentBatch, sink);

			currentSolMapsForRequest.clear();
			currentBatch.clear();

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
				numberOfOutputMappingsProduced++;
				sink.send(inputSolMap);
			}

			return;
		}

		// At this point, we know that we may retrieve join partners for
		// the input solution mapping. Therefore, the following function
		// makes sure that we will consider the input solution mapping in
		// the next bind-join request that we will do.
		_processJoinableInput(inputSolMap, restrictedInputSolMap, sink, execCxt);
	}

	/**
	 * Makes sure that the given solution mapping will be considered for the
	 * next bind-join request, and performs that request if enough solution
	 * mappings have been accumulated.
	 *
	 * @param inputSolMap - the solution mapping to be considered; at this
	 *             point, we assume that this solution mapping covers at
	 *             least one join variable and does not assign a blank node
	 *             to any of the join variables
	 *
	 * @param inputSolMapRestricted - a version of inputSolMap that is
	 *             restricted to the join variables
	 */
	protected void _processJoinableInput( final SolutionMapping inputSolMap,
	                                      final Binding inputSolMapRestricted,
	                                      final IntermediateResultElementSink sink,
	                                      final ExecutionContext execCxt )
			 throws ExecOpExecutionException
	{
		// Add the given solution mapping to the batch of solution mappings
		// considered by the next bind-join request.
		currentBatch.add(inputSolMap);

		// Check whether the restricted version of the given input solution
		// mapping is already covered by the set of solution mappings from
		// which the next bind-join request will be formed.
		if ( ! alreadyCovered(inputSolMapRestricted) ) {
			// If it is not covered, we need to add it to the set, but first
			// we may have to remove solution mappings from that set.
			if ( ! allJoinVarsAreCertain ) {
				// Update the set of solution mappings already collected for
				// the request by removing the solution mappings that include
				// the given restricted solution mapping.
				// This is okay because the potential join partners captured
				// by these solution mappings are also captured by the given
				// restricted solution mapping, which we will add to the set
				// in the next step. In fact, it is even necessary to do so
				// in order to avoid spurious duplicates in the join result.
				currentSolMapsForRequest.removeIf( sm -> SolutionMappingUtils.includedIn(inputSolMapRestricted, sm) );
			}

			// Now we add it to the set.
			currentSolMapsForRequest.add(inputSolMapRestricted);
		}

		// If we have accumulated enough solution mappings for the next
		// bind-join request, then let's perform this request.
		if ( currentSolMapsForRequest.size() == requestBlockSize ) {
			performRequestAndHandleResponse(sink, execCxt);

			// After performing the request (and handling its response), we can
			// forget about the solution mappings considered for the request.
			currentSolMapsForRequest.clear();
			currentBatch.clear();
		}
	}

	protected void performRequestAndHandleResponse( final IntermediateResultElementSink sink,
	                                                final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableReqOp(currentSolMapsForRequest);

		// This sink will collect the solution mappings obtained by the
		// bind-join request and immediately join them with the input
		// solution mappings that we have accumulated in 'currentBatch'.
		// Once the request has been executed, we will send the resulting
		// joined solution mappings from 'mySink' to 'sink' (see below).
		final MyIntermediateResultElementSink mySink = createMySink();

		numberOfRequestOpsUsed++;

		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
// TODO: How to (re)implement this part now?
//			final boolean requestBlockSizeReduced = reduceRequestBlockSize();
//			if ( requestBlockSizeReduced && ! mySink.hasObtainedInputAlready() ) {
//				// If the request operator did not yet sent any solution
//				// mapping to the sink, then we can retry to process the
//				// given list of input solution mappings with the reduced
//				// request block size.
//				_processBatch(joinableInputSMs, mySink, execCxt);
//			}
//			else {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
//			}
		}

		// Now we send the complete set of resulting joined solution mappings
		// from 'mySink' to 'sink'. Sending them as a single chunk is useful
		// because it reduces communication between the threads that run this
		// and the next operator of the query plan.
		consumeMySink(mySink, sink);

		statsOfLastReqOp = reqOp.getStats();
		if ( statsOfFirstReqOp == null ) statsOfFirstReqOp = statsOfLastReqOp;
	}

	protected boolean alreadyCovered( final Binding inputSolMapRestricted ) {
		if ( currentSolMapsForRequest.contains(inputSolMapRestricted) ) {
			return true;
		}

		if ( ! allJoinVarsAreCertain ) {
			for ( final Binding sm : currentSolMapsForRequest ) {
				if ( SolutionMappingUtils.includedIn(sm, inputSolMapRestricted) ) {
					return true;
				}
			}
		}

		return false;
	}

	protected MyIntermediateResultElementSink createMySink() {
		if ( useOuterJoinSemantics )
			return new MyIntermediateResultElementSinkOuterJoin(currentBatch);
		else
			return new MyIntermediateResultElementSink(currentBatch);
	}

	protected void consumeMySink( final MyIntermediateResultElementSink mySink,
	                              final IntermediateResultElementSink outputSink ) {
		mySink.flush();

		final List<SolutionMapping> output = mySink.getSolMapsForOutput();
		if ( ! output.isEmpty() ) {
			numberOfOutputMappingsProduced += output.size();
			outputSink.send(output);
		}
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( fullResult == null && ! currentSolMapsForRequest.isEmpty() ) {
			performRequestAndHandleResponse(sink, execCxt);
		}
	}

	/**
	 * Reduces {@link #requestBlockSize} to its current value divided
	 * by 2 if the resulting value would still be greater or equal to
	 * {@link #minimumRequestBlockSize}. In this case, this function
	 * returns <code>true</code>. Otherwise, the function returns
	 * <code>false</code> without reducing {@link #requestBlockSize}.
	 */
	protected boolean reduceRequestBlockSize() {
		final int newRequestBlockSize = requestBlockSize / 2;
		if ( newRequestBlockSize < minimumRequestBlockSize ) {
			return false;
		}
		else {
			requestBlockSize = newRequestBlockSize;
			requestBlockSizeWasReduced = true;
			return true;
		}
	}

	/**
	 * Implementations of this function should create an executable operator
	 * that can perform a bind join request in which the query of this bind
	 * join operator is combined with the given solution mappings.
	 *
	 * Implementations can assume that the given solution mappings are already
	 * restricted to contain bindings only for the join variables, that none
	 * of the given solution mappings contains blank nodes, that none of the
	 * given solution mappings is the empty solution mapping, and that the
	 * given set of solution mappings is duplicate free and nonempty.
	 *
	 * The operator created by this function should throw exceptions instead
	 * of collecting them.
	 */
	protected abstract NullaryExecutableOp createExecutableReqOp( Set<Binding> solMaps );


	// ------- functionality for Stats ------

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
		requestBlockSizeWasReduced = false;
		numberOfRequestOpsUsed = 0;
		statsOfFirstReqOp = null;
		statsOfLastReqOp = null;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "queryAsString",      query.toString() );
		s.put( "fedMemberAsString",  fm.toString() );
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
		s.put( "hadToSwitchToFullRetrievalMode",  Boolean.valueOf(fullResult != null) );
		s.put( "requestBlockSizeWasReduced",      Boolean.valueOf(requestBlockSizeWasReduced) );
		s.put( "requestBlockSize",                Integer.valueOf(requestBlockSize) );
		s.put( "numberOfRequestOpsUsed",          Integer.valueOf(numberOfRequestOpsUsed) );

		if ( statsOfFirstReqOp == statsOfLastReqOp ) {
			s.put( "statsOfReqOp",  statsOfFirstReqOp );
		}
		else {
			s.put( "statsOfFirstReqOp",  statsOfFirstReqOp );
			s.put( "statsOfLastReqOp",   statsOfLastReqOp );
		}

		return s;
	}


	// ------- helper classes ------

	protected class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final Iterable<SolutionMapping> inputSolutionMappings;
		protected final List<SolutionMapping> solMapsForOutput = new ArrayList<>();
		private boolean inputObtained = false;

		public MyIntermediateResultElementSink( final Iterable<SolutionMapping> inputSolutionMappings ) {
			this.inputSolutionMappings = inputSolutionMappings;
		}

		@Override
		public final void send( final SolutionMapping smFromRequest ) {
			inputObtained = true;
			_send(smFromRequest);
		}

		protected void _send( final SolutionMapping smFromRequest ) {
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					solMapsForOutput.add( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
				}
			}
		}

		public void flush() { }

		public final boolean hasObtainedInputAlready() { return inputObtained; }

		public List<SolutionMapping> getSolMapsForOutput() { return solMapsForOutput; }

	} // end of helper class MyIntermediateResultElementSink


	protected class MyIntermediateResultElementSinkOuterJoin extends MyIntermediateResultElementSink
	{
		protected final Set<SolutionMapping> inputSolutionMappingsWithJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSinkOuterJoin( final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(inputSolutionMappings);
		}

		@Override
		public void _send( final SolutionMapping smFromRequest ) {
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					solMapsForOutput.add( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
					inputSolutionMappingsWithJoinPartners.add(smFromInput);
				}
			}
		}

		/**
		 * Sends to the output sink all input solution
		 * mappings that did not have a join partner.
		 */
		@Override
		public void flush() {
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( ! inputSolutionMappingsWithJoinPartners.contains(smFromInput) ) {
					solMapsForOutput.add(smFromInput);
				}
			}
		}

	} // end of helper class MyIntermediateResultElementSinkOuterJoin


	// ------- functionality for full-retrieval mode ------

	/**
	 * Performs a request to retrieve all solution mappings for the query
	 * of this operator (see {@link #createExecutableReqOpForAll}) and puts
	 * the retrieved solution mappings into {@link #fullResult}.
	 */
	protected void switchToFullRetrievalMode( final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableReqOpForAll();
		numberOfRequestOpsUsed++;

		final CollectingIntermediateResultElementSink mySink = new CollectingIntermediateResultElementSink();
		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
			throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
		}

		statsOfLastReqOp = reqOp.getStats();
		if ( statsOfFirstReqOp == null ) statsOfFirstReqOp = statsOfLastReqOp;

		fullResult = mySink.getCollectedSolutionMappings();
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
		boolean hasJoinPartners = false;
		for ( final SolutionMapping retrievedSM : fullResult ) {
			if ( SolutionMappingUtils.compatible(retrievedSM, inputSolMap) ) {
				hasJoinPartners = true;
				numberOfOutputMappingsProduced++;
				sink.send( SolutionMappingUtils.merge(retrievedSM, inputSolMap) );
			}
		}

		if ( useOuterJoinSemantics && ! hasJoinPartners ) {
			numberOfOutputMappingsProduced++;
			sink.send(inputSolMap);
		}
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

}
