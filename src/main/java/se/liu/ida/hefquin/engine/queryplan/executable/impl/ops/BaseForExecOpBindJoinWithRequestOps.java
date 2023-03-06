package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.utils.Pair;

/**
 * Abstract base class to implement bind joins by using request operators.
 *
 * Instead of simply using every input block of solution mappings to directly
 * create a corresponding bind-join request, this implementation can split the
 * input block into smaller blocks for the requests. On top of that, this
 * implementation automatically reduces the block size for requests in case
 * a request operator fails and, then, the implementation even tries to
 * re-process (with the reduced request block size) the input solution
 * mappings for which the request operator failed.
 *
 * A potential downside of this capability is that, if this algorithm has
 * to execute multiple requests per input block, then these requests are
 * executed sequentially.
 */
public abstract class BaseForExecOpBindJoinWithRequestOps<QueryType extends Query,
                                                          MemberType extends FederationMember>
           extends BaseForExecOpBindJoin<QueryType,MemberType>
{
	protected final boolean useOuterJoinSemantics;

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

	public BaseForExecOpBindJoinWithRequestOps( final QueryType query,
	                                            final MemberType fm,
	                                            final boolean useOuterJoinSemantics,
	                                            final boolean collectExceptions ) {
		super(query, fm, collectExceptions);
		this.useOuterJoinSemantics = useOuterJoinSemantics;
		this.requestBlockSize = preferredInputBlockSize();
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final Pair<List<SolutionMapping>, List<SolutionMapping>> splitInput = extractUnjoinableInputSMs( input.getSolutionMappings() );
		final List<SolutionMapping> unjoinableInputSMs = splitInput.object1;
		final List<SolutionMapping> joinableInputSMs   = splitInput.object2;

		if ( useOuterJoinSemantics ) {
			for ( final SolutionMapping sm : unjoinableInputSMs ) {
				numberOfOutputMappingsProduced++;
				sink.send(sm);
			}
		}

		_process( joinableInputSMs, sink, execCxt );
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
			return true;
		}
	}

	/**
	 * Splits the given collection of solution mappings into two such that the
	 * first list contains all the solution mappings that are guaranteed not to
	 * have any join partner and the second list contains the rest of the given
	 * input solution mappings (which may have join partners). Typically, the
	 * solution mappings that are guaranteed not to have join partners are the
	 * ones that have a blank node for one of the join variables.
	 */
	protected abstract Pair<List<SolutionMapping>, List<SolutionMapping>> extractUnjoinableInputSMs( Iterable<SolutionMapping> solMaps );

	/**
	 * The returned operator should be created such that it throws exceptions
	 * instead of collecting them.
	 */
	protected abstract NullaryExecutableOp createExecutableRequestOperator( Iterable<SolutionMapping> solMaps );

	/**
	 * This function may be used to implement {@link #extractUnjoinableInputSMs(Iterable).
	 */
	protected Pair<List<SolutionMapping>, List<SolutionMapping>> extractUnjoinableInputSMs( final Iterable<SolutionMapping> solMaps,
	                                                                                        final Iterable<Var> potentialJoinVars ) {
		final List<SolutionMapping> unjoinable = new ArrayList<>();
		final List<SolutionMapping> joinable   = new ArrayList<>();

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

		return new Pair<>(unjoinable, joinable);
	}


	@Override
	public void resetStats() {
		super.resetStats();
		numberOfOutputMappingsProduced = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfOutputMappingsProduced",  Long.valueOf(numberOfOutputMappingsProduced) );
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
