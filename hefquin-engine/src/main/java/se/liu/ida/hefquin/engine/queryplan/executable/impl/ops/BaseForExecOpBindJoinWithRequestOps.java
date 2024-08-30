package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * A generic implementation of the bind join algorithm that uses executable
 * request operators for performing the requests to the federation member.
 *
 * The implementation is generic in the sense that it works with any type of
 * request operator. Each concrete implementation that extends this base class
 * needs to implement the {@link #createExecutableRequestOperator(Iterable)}
 * function to create the request operators with the types of requests that
 * are specific to that concrete implementation.
 *
 * This implementation is capable of separating out each input solution mapping
 * that assigns a blank node to any of the join variables. Then, such solution
 * mappings are not even considered when creating the requests because they
 * cannot have any join partners in the results obtained from the federation
 * member. Of course, in case the algorithm is used under outer-join semantics
 * these solution mappings are still returned to the output (without joining
 * them with anything).
 *
 * Another capability of this implementation is that, instead of simply using
 * every input block of solution mappings to directly create a corresponding
 * bind-join request, this implementation can split the input block into
 * smaller blocks for the requests. On top of that, in case a request operator
 * fails, this implementation automatically reduces the block size for requests 
 * and, then, tries to re-process (with the reduced request block size) the
 * input solution mappings for which the request operator failed.
 *
 * A potential downside of the latter capability is that, if this algorithm
 * has to execute multiple requests per input block, then these requests are
 * executed sequentially.
 */
public abstract class BaseForExecOpBindJoinWithRequestOps<QueryType extends Query,
                                                          MemberType extends FederationMember>
           extends BaseForExecOpBindJoin<QueryType,MemberType>
{
	protected final boolean useOuterJoinSemantics;
	protected final Set<Var> varsInPatternForFM;

	/**
	 * The number of solution mappings that this operator uses for each
	 * of the bind join requests. This number may be adapted at runtime.
	 */
	protected int requestBlockSize;

	/**
	 * The minimum value to which {@link #requestBlockSize} can be reduced.
	 */
	protected static final int minimumRequestBlockSize = 5;

	// statistics
	private long numberOfOutputMappingsProduced = 0L;
	protected boolean requestBlockSizeWasReduced = false;
	protected int numberOfRequestOpsUsed = 0;
	protected ExecutableOperatorStats statsOfFirstReqOp = null;
	protected ExecutableOperatorStats statsOfLastReqOp = null;

	/**
	 * @param varsInPatternForFM
	 *             may be used by sub-classes to provide the set of variables
	 *             that occur in the graph pattern that the bind join evaluates
	 *             at the federation member; sub-classes that cannot extract
	 *             this set in their constructor may pass <code>null</code> as
	 *             value for this argument; if provided, this implementation
	 *             can filter out input solution mappings that contain blank
	 *             nodes for the join variables and, thus, cannot be joined
	 *             with the solution mappings obtained via the requests
	 */
	public BaseForExecOpBindJoinWithRequestOps( final QueryType query,
	                                            final MemberType fm,
	                                            final boolean useOuterJoinSemantics,
	                                            final Set<Var> varsInPatternForFM,
	                                            final boolean collectExceptions ) {
		super(query, fm, collectExceptions);
		this.useOuterJoinSemantics = useOuterJoinSemantics;
		this.varsInPatternForFM = varsInPatternForFM;
		this.requestBlockSize = preferredInputBlockSize();
	}

	public BaseForExecOpBindJoinWithRequestOps( final QueryType query,
	                                            final MemberType fm,
	                                            final boolean useOuterJoinSemantics,
	                                            final boolean collectExceptions ) {
		this(query, fm, useOuterJoinSemantics, null, collectExceptions);
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final Iterable<SolutionMapping> inputSMsForJoin;
		if ( varsInPatternForFM != null ) {
			final List<SolutionMapping> unjoinableInputSMs = new ArrayList<>();
			final List<SolutionMapping> joinableInputSMs = new ArrayList<>();

			extractUnjoinableInputSMs( input.getSolutionMappings(),
			                           varsInPatternForFM,
			                           unjoinableInputSMs,
			                           joinableInputSMs );

			if ( useOuterJoinSemantics ) {
				for ( final SolutionMapping sm : unjoinableInputSMs ) {
					numberOfOutputMappingsProduced++;
					sink.send(sm);
				}
			}

			inputSMsForJoin = joinableInputSMs;
		}
		else {
			inputSMsForJoin = input.getSolutionMappings();
		}

		_process( inputSMsForJoin, sink, execCxt );
	}

	protected void _process( final Iterable<SolutionMapping> joinableInputSMs,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final Iterator<SolutionMapping> it = joinableInputSMs.iterator();
		while ( it.hasNext() ) {
			// create next chunk of input solution mappings to be processed
			final List<SolutionMapping> nextChunkOfInput = new ArrayList<>(requestBlockSize);
			int chunkSize = 0;
			while ( chunkSize < requestBlockSize && it.hasNext() ) {
				nextChunkOfInput.add( it.next() );
				chunkSize++;
			}

			// process the created chunk of input solution mappings
			_processWithoutSplittingInputFirst(nextChunkOfInput, sink, execCxt);
		}
	}

	protected void _processWithoutSplittingInputFirst( final List<SolutionMapping> joinableInputSMs,
	                                                   final IntermediateResultElementSink sink,
	                                                   final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator(joinableInputSMs);

		if ( reqOp != null ) {
			numberOfRequestOpsUsed++;

			final MyIntermediateResultElementSink mySink;
			if ( useOuterJoinSemantics )
				mySink = new MyIntermediateResultElementSinkOuterJoin(sink, joinableInputSMs);
			else
				mySink = new MyIntermediateResultElementSink(sink, joinableInputSMs);

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
					_process(joinableInputSMs, mySink, execCxt);
				}
				else {
					throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
				}
			}

			mySink.flush();

			statsOfLastReqOp = reqOp.getStats();
			if ( statsOfFirstReqOp == null ) statsOfFirstReqOp = statsOfLastReqOp;
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
	 * The returned operator should be created such that it throws exceptions
	 * instead of collecting them.
	 */
	protected abstract NullaryExecutableOp createExecutableRequestOperator( Iterable<SolutionMapping> solMaps );

	/**
	 * Splits the given collection of solution mappings into two such that the
	 * first list contains all the solution mappings that are guaranteed not to
	 * have any join partner and the second list contains the rest of the given
	 * input solution mappings (which may have join partners). Typically, the
	 * solution mappings that are guaranteed not to have join partners are the
	 * ones that have a blank node for one of the join variables.
	 */
	protected void extractUnjoinableInputSMs( final Iterable<SolutionMapping> solMaps,
	                                          final Iterable<Var> potentialJoinVars,
	                                          final List<SolutionMapping> unjoinable,
	                                          final List<SolutionMapping> joinable ) {
		for ( final SolutionMapping sm : solMaps) {
			boolean isJoinable = true;
			for ( final Var joinVar : potentialJoinVars ) {
				final Node joinValue = sm.asJenaBinding().get(joinVar);
				if ( joinValue != null && joinValue.isBlank() ) {
					isJoinable = false;
					break;
				}
			}

			if ( isJoinable )
				joinable.add(sm);
			else
				unjoinable.add(sm);
		}
	}


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
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
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
		protected final IntermediateResultElementSink outputSink;
		protected final Iterable<SolutionMapping> inputSolutionMappings;
		private boolean inputObtained = false;

		public MyIntermediateResultElementSink( final IntermediateResultElementSink outputSink,
		                                        final Iterable<SolutionMapping> inputSolutionMappings ) {
			this.outputSink = outputSink;
			this.inputSolutionMappings = inputSolutionMappings;
		}

		@Override
		public final void send( final SolutionMapping smFromRequest ) {
			inputObtained = true;
			_send(smFromRequest);
		}

		protected void _send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of inputSolutionMappings that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					numberOfOutputMappingsProduced++;
					outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
				}
			}
		}

		public void flush() { }

		public final boolean hasObtainedInputAlready() { return inputObtained; }

	} // end of helper class MyIntermediateResultElementSink


	protected class MyIntermediateResultElementSinkOuterJoin extends MyIntermediateResultElementSink
	{
		protected final Set<SolutionMapping> inputSolutionMappingsWithJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSinkOuterJoin( final IntermediateResultElementSink outputSink,
		                                                 final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(outputSink, inputSolutionMappings);
		}

		@Override
		public void _send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of inputSolutionMappings that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					numberOfOutputMappingsProduced++;
					outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
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
					numberOfOutputMappingsProduced++;
					outputSink.send(smFromInput);
				}
			}
		}

	} // end of helper class MyIntermediateResultElementSinkOuterJoin

}
