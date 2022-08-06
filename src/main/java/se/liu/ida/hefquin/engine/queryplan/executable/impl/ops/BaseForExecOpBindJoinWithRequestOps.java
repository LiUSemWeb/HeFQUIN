package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
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
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.utils.Pair;

/**
 * Abstract base class to implement bind joins by using request operators.
 *
 * Note that executing the request operator is a blocking operation within
 * the algorithm implemented by this class. However, it does not matter
 * because this bind join algorithm uses only one request for any given
 * {@link IntermediateResultBlock}. Issuing the request directly (and then
 * using a response processor) would also be blocking because we would have
 * to wait for the response processor. Attention: things may look different
 * if we have to do multiple requests per {@link IntermediateResultBlock},
 * which may be the case if the block size is greater than what the
 * server can/wants to handle.
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

	public BaseForExecOpBindJoinWithRequestOps( final QueryType query,
	                                            final MemberType fm,
	                                            final boolean useOuterJoinSemantics ) {
		super(query, fm);
		this.useOuterJoinSemantics = useOuterJoinSemantics;
		this.requestBlockSize = preferredInputBlockSize();
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final Pair<List<SolutionMapping>, List<SolutionMapping>> splitInput = extractUnjoinableInputSMs( input.getSolutionMappings() );
		final List<SolutionMapping> unjoinableInputSMs = splitInput.object1;
		final List<SolutionMapping> joinableInputSMs   = splitInput.object2;

		if ( useOuterJoinSemantics ) {
			for ( final SolutionMapping sm : unjoinableInputSMs ) {
				sink.send(sm);
			}
		}

		_process( joinableInputSMs, sink, execCxt );
	}

	protected void _process( final Iterable<SolutionMapping> joinableInputSMs,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt)
			throws ExecOpExecutionException
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
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}

			mySink.flush();
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


	// ------- helper classes ------

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final Iterable<SolutionMapping> inputSolutionMappings;

		public MyIntermediateResultElementSink( final IntermediateResultElementSink outputSink,
		                                        final Iterable<SolutionMapping> inputSolutionMappings ) {
			this.outputSink = outputSink;
			this.inputSolutionMappings = inputSolutionMappings;
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of inputSolutionMappings that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
					outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
				}
			}
		}

		public void flush() { }

	} // end of helper class MyIntermediateResultElementSink


	protected static class MyIntermediateResultElementSinkOuterJoin extends MyIntermediateResultElementSink
	{
		protected final Set<SolutionMapping> inputSolutionMappingsWithJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSinkOuterJoin( final IntermediateResultElementSink outputSink,
		                                                 final Iterable<SolutionMapping> inputSolutionMappings ) {
			super(outputSink, inputSolutionMappings);
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of inputSolutionMappings that can
			// be used like an index.
			// See: https://github.com/LiUSemWeb/HeFQUIN/issues/3
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(smFromInput, smFromRequest) ) {
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
					outputSink.send(smFromInput);
				}
			}
		}

	} // end of helper class MyIntermediateResultElementSinkOuterJoin

}
