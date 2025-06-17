package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

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
 * Then, for every batch of solution mappings from the input, the algorithm
 * creates the corresponding request (see above) and sends this request to
 * the federation member (the algorithm may even decide to split the input
 * batch into smaller batches for multiple requests; see below). The response
 * to such a request is the subset of the solutions for the query/pattern of
 * this operator that are join partners for at least one of the solutions that
 * were used for creating the request. After receiving such a response, the
 * algorithm locally joins the solutions from the response with the solutions
 * in the batch used for creating the request, and outputs the resulting
 * joined solutions (if any). Thereafter, the algorithm moves on to the next
 * batch of solutions from the input.
 *
 * This implementation is capable of separating out each input solution mapping
 * that assigns a blank node to any of the join variables. Then, such solution
 * mappings are not even considered when creating the requests because they
 * cannot have any join partners in the results obtained from the federation
 * member. Of course, in case the algorithm is used with outer-join semantics,
 * these solution mappings are still returned to the output (without joining
 * them with anything).
 *
 * Another capability of this implementation is that, instead of simply using
 * every input batch of solution mappings to directly create a corresponding
 * bind-join request, this implementation can split the input batch into
 * smaller batches for the requests. On top of that, in case a request operator
 * fails, this implementation automatically reduces the batch size for requests
 * and, then, tries to re-process (with the reduced request batch size) the
 * input solution mappings for which the request operator failed.
 *
 * A potential downside of the latter capability is that, if this algorithm
 * has to execute multiple requests per input batch, then these requests are
 * executed sequentially.
 *
 * Another capability of this implementation is that it can switch into a
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
           extends UnaryExecutableOpBaseWithBatching
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
	                                            final boolean collectExceptions ) {
		super(batchSize, collectExceptions);

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

	private final List<SolutionMapping> unjoinableInputSMs = new ArrayList<>();
	private final List<SolutionMapping> joinableInputSMs = new ArrayList<>();

	@Override
	protected void _processBatch( final List<SolutionMapping> batchOfSolMaps,
	                              final IntermediateResultElementSink sink,
	                              final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		// If the operator had to switch into full-retrieval mode, we can
		// now also use the retrieved (full) result for the given batch.
// TODO: In this case, we may want to skip the batching completely and, thus,
// override the '_process' method of the base class (UnaryExecutableOpBaseWithBatching).
// But this should also work in combination with ExecOpBindJoinSPARQLwithVALUESorFILTER.
		if ( fullResult != null ) {
			joinInFullRetrievalMode(batchOfSolMaps, sink);
			return;
		}

		unjoinableInputSMs.clear();
		joinableInputSMs.clear();

		final boolean allHaveJoinVars = extractUnjoinableInputSMs( batchOfSolMaps,
		                                                           varsInQuery,
		                                                           unjoinableInputSMs,
		                                                           joinableInputSMs );

		if ( ! allHaveJoinVars ) {
			switchToFullRetrievalMode(execCxt);
			joinInFullRetrievalMode(batchOfSolMaps, sink);
		}
		else {
			if ( useOuterJoinSemantics ) {
				numberOfOutputMappingsProduced += unjoinableInputSMs.size();
				sink.send(unjoinableInputSMs);
			}

			_processJoinableInput( joinableInputSMs, sink, execCxt );
		}

		unjoinableInputSMs.clear();
		joinableInputSMs.clear();
	}

	@Override
	protected void _concludeExecution( final List<SolutionMapping> batchOfSolMaps,
	                                   final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		if ( batchOfSolMaps != null && ! batchOfSolMaps.isEmpty() ) {
			_processBatch(batchOfSolMaps, sink, execCxt);
		}
	}

	private final List<SolutionMapping> nextChunkOfInput = new ArrayList<>();

	protected void _processJoinableInput( final Iterable<SolutionMapping> joinableInputSMs,
	                                      final IntermediateResultElementSink sink,
	                                      final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final Iterator<SolutionMapping> it = joinableInputSMs.iterator();
		while ( it.hasNext() ) {
			// create next chunk of input solution mappings to be processed
			nextChunkOfInput.clear();
			while ( nextChunkOfInput.size() < requestBlockSize && it.hasNext() ) {
				nextChunkOfInput.add( it.next() );
			}

			// process the created chunk of input solution mappings
			_processWithoutSplittingInputFirst(nextChunkOfInput, sink, execCxt);
		}

		nextChunkOfInput.clear();
	}

	protected void _processWithoutSplittingInputFirst( final List<SolutionMapping> joinableInputSMs,
	                                                   final IntermediateResultElementSink sink,
	                                                   final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		// Strip the given solution mappings of all bindings for non-join
		// variables (as we do not need to ship non-join variables in the 
		// bind-join requests). Additionally, for uses of this operator in
		// cases in which some join variables may be possible variables (not
		// certain variables), we also need to make sure to exclude solution
		// mappings that are covered by other solution mappings to be shipped
		// (otherwise, we may end up with spurious duplicates in the result
		// of this operator).
		final Set<Binding> solMapsForRequest;
		if ( allJoinVarsAreCertain )
			solMapsForRequest = removeNonJoinVars(joinableInputSMs);
		else
			solMapsForRequest = removeNonJoinVarsAndExcludeCoveredSolMaps(joinableInputSMs);

		assert ! solMapsForRequest.isEmpty();

		final NullaryExecutableOp reqOp = createExecutableReqOp(solMapsForRequest);

		numberOfRequestOpsUsed++;

		final MyIntermediateResultElementSink mySink;
		if ( useOuterJoinSemantics )
			mySink = new MyIntermediateResultElementSinkOuterJoin(joinableInputSMs);
		else
			mySink = new MyIntermediateResultElementSink(joinableInputSMs);

		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
			final boolean requestBlockSizeReduced = reduceRequestBlockSize();
			if ( requestBlockSizeReduced && ! mySink.hasObtainedInputAlready() ) {
				// If the request operator did not yet sent any solution
				// mapping to the sink, then we can retry to process the
				// given list of input solution mappings with the reduced
				// request block size.
				_processBatch(joinableInputSMs, mySink, execCxt);
			}
			else {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}
		}

		mySink.flush();

		final List<SolutionMapping> output = mySink.getSolMapsForOutput();
		if ( ! output.isEmpty() ) {
			numberOfOutputMappingsProduced += output.size();
			sink.send(output);
		}

		statsOfLastReqOp = reqOp.getStats();
		if ( statsOfFirstReqOp == null ) statsOfFirstReqOp = statsOfLastReqOp;
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

	/**
	 * Splits the given collection of solution mappings into two such that the
	 * first list contains all the solution mappings that are guaranteed not to
	 * have any join partner and the second list contains the rest of the given
	 * input solution mappings (which may have join partners). Typically, the
	 * solution mappings that are guaranteed not to have join partners are the
	 * ones that have a blank node for one of the join variables.
	 *
	 * Returns <code>true</code> if each of the given solution mappings
	 * has a binding for at least one of the join variables. As soon as
	 * this function comes across a solution mapping for which this is
	 * not the case (i.e., a solution mapping that doesn't cover any of
	 * the join variables), the function terminates immediately and
	 * returns <code>false</code> (i.e., in this case, the two lists
	 * to be populated by this function may be incomplete).
	 */
	protected boolean extractUnjoinableInputSMs( final Iterable<SolutionMapping> solMaps,
	                                             final Iterable<Var> potentialJoinVars,
	                                             final List<SolutionMapping> unjoinable,
	                                             final List<SolutionMapping> joinable ) {
		for ( final SolutionMapping sm : solMaps ) {
			boolean isJoinable = true;
			boolean hasJoinVariable = false;
			for ( final Var joinVar : potentialJoinVars ) {
				final Node joinValue = sm.asJenaBinding().get(joinVar);

				if ( joinValue != null ) {
					hasJoinVariable = true;
				}

				if ( joinValue != null && joinValue.isBlank() ) {
					isJoinable = false;
					break;
				}
			}

			if ( hasJoinVariable == false ) {
				return false;
			}

			if ( isJoinable )
				joinable.add(sm);
			else
				unjoinable.add(sm);
		}

		return true;
	}

	/**
	 * Returns a set of versions of the given solution mappings in which
	 * bindings for non-join variables are removed. While projecting away
	 * the non-join variables may result in solution mappings that are
	 * duplicates of one another, such duplicates are not contained in
	 * the returned set.
	 *
	 * Assumes that all of the given solution mappings have a binding for
	 * at least one join variable and that none of them binds a blank node
	 * to any of the join variables.
	 */
	protected Set<Binding> removeNonJoinVars( final Iterable<SolutionMapping> joinableSolMaps ) {
		// We collect the restricted solution mappings as a set to avoid duplicates.
		final Set<Binding> restrictedSolMaps = new HashSet<>();

		for ( final SolutionMapping s : joinableSolMaps ) {
			final Binding b = SolutionMappingUtils.restrict( s.asJenaBinding(),
			                                                 varsInQuery );

			if ( b.isEmpty() ) {
				// Such a solution mapping should not anymore be in the
				// process at this point. If there was such a solution
				// mapping, it should have been detected already while
				// executing the extractUnjoinableInputSMs function.
				throw new IllegalStateException("Solution mapping without join variables, which should have been detected and handled earlier (the solution mapping is: " + b.toString() + ").");
			}

			restrictedSolMaps.add(b);
		}

		return restrictedSolMaps;
	}

	/**
	 * Returns a set of versions of the given solution mappings in which
	 * bindings for non-join variables are removed, and none of the returned
	 * solution mappings is covered by any other of the return solution
	 * mappings.
	 *
	 * While projecting away the non-join variables may result in solution
	 * mappings that are duplicates of one another, such duplicates are not
	 * contained in the returned set.
	 *
	 * Assumes that all of the given solution mappings have a binding for
	 * at least one join variable and that none of them binds a blank node
	 * to any of the join variables.
	 */
	protected Set<Binding> removeNonJoinVarsAndExcludeCoveredSolMaps( final Iterable<SolutionMapping> joinableSolMaps ) {
		// We first collect the restricted solution mappings into different
		// sets, depending on the number of (join) variables that they bind.
		// Separating them in this way will make it easier (and more efficient)
		// to search for the ones that are covered by others.
		// - the sets for restricted solution mappings that contain one / two /
		//   three bindings (each of them will be created if needed); we use
		//   these ones only to avoid having to always access the following
		//   map for every restricted solution mapping
		Set<Binding> restrictedSolMaps1 = null;
		Set<Binding> restrictedSolMaps2 = null;
		Set<Binding> restrictedSolMaps3 = null;
		// - the sets for restricted solution mappings hashed by the number
		//   of bindings, including each of the previous three (if created)
		final Map<Integer,Set<Binding>> mapOfSolMapSets = new HashMap<>();

		for ( final SolutionMapping s : joinableSolMaps ) {
			final Binding b = SolutionMappingUtils.restrict( s.asJenaBinding(), varsInQuery );

			if ( b.isEmpty() ) {
				// Such a solution mapping should not anymore be in the
				// process at this point. If there was such a solution
				// mapping, it should have been detected already while
				// executing the extractUnjoinableInputSMs function.
				throw new IllegalStateException("Solution mapping without join variables, which should have been detected and handled earlier (the solution mapping is: " + b.toString() + ").");
			}

			if ( b.size() == 1 ) {
				if ( restrictedSolMaps1 == null ) {
					restrictedSolMaps1 = new HashSet<>();
					mapOfSolMapSets.put(1, restrictedSolMaps1);
				}

				restrictedSolMaps1.add(b);
			}
			else if ( b.size() == 2 ) {
				if ( restrictedSolMaps2 == null ) {
					restrictedSolMaps2 = new HashSet<>();
					mapOfSolMapSets.put(2, restrictedSolMaps2);
				}

				restrictedSolMaps2.add(b);
			}
			else if ( b.size() == 3 ) {
				if ( restrictedSolMaps3 == null ) {
					restrictedSolMaps3 = new HashSet<>();
					mapOfSolMapSets.put(3, restrictedSolMaps3);
				}

				restrictedSolMaps3.add(b);
			}
			else { 
				if ( ! mapOfSolMapSets.containsKey(b.size()) )
					mapOfSolMapSets.put( b.size(), new HashSet<>() );

				mapOfSolMapSets.get( b.size() ).add(b);
			}
		}

		assert ! mapOfSolMapSets.isEmpty();

		// The remainder of this function removes solution mappings
		// that are covered by other solution mappings.

		// First, find the first nonempty set.
		int numberOfCurrentSet = 1;
		while ( ! mapOfSolMapSets.containsKey(numberOfCurrentSet) ) {
			numberOfCurrentSet++;
		}

		// The solution mappings in the first nonempty set are certainly not
		// covered by any of the solution mappings. Hence, these can be carried
		// over to the output without checking.
		final Set<Binding> resultingSolMaps = mapOfSolMapSets.remove(numberOfCurrentSet);

		// Now, for each of the remaining nonempty sets, iterate over the
		// solution mappings in the set. For each such solution mapping,
		// carry it over to the output if there is no solution mapping
		// carried over from the previous sets that covers it.
		while ( ! mapOfSolMapSets.isEmpty() ) {
			numberOfCurrentSet++;
			final Set<Binding> currentSet = mapOfSolMapSets.remove(numberOfCurrentSet);

			if ( currentSet != null ) {
				// Collect the solution mappings from the current set that are
				// not covered by any of the solution mappings that are already
				// carried over to the output.
				final Set<Binding> nonCovered = new HashSet<>();
				for ( final Binding candidate : currentSet ) {
					final Iterator<Binding> itOutput = resultingSolMaps.iterator();
					boolean isCovered = false;
					while ( ! isCovered && itOutput.hasNext() ) {
						isCovered = SolutionMappingUtils.covers( itOutput.next(),
						                                         candidate );
					}

					if ( isCovered == false ) {
						nonCovered.add(candidate);
					}
				}

				// Carry the collected solution mappings over to the output.
				resultingSolMaps.addAll(nonCovered);
				currentSet.clear();
			}
		}

		return resultingSolMaps;
	}


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
		s.put( "statsOfFirstReqOp",  statsOfFirstReqOp );
		s.put( "statsOfLastReqOp",   statsOfLastReqOp );
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

	protected void joinInFullRetrievalMode( final List<SolutionMapping> batchOfSolMaps,
	                                        final IntermediateResultElementSink sink )
	{
		for ( final SolutionMapping inputSM : batchOfSolMaps ) {
			boolean hasJoinPartners = false;
			for ( final SolutionMapping retrievedSM : fullResult ) {
				if ( SolutionMappingUtils.compatible(retrievedSM, inputSM) ) {
					hasJoinPartners = true;
					numberOfOutputMappingsProduced++;
					sink.send( SolutionMappingUtils.merge(retrievedSM, inputSM) );
				}
			}

			if ( useOuterJoinSemantics && ! hasJoinPartners ) {
				sink.send(inputSM);
			}
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
