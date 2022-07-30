package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericOuterBindJoinWithRequestOps<QueryType extends Query,
                                                               MemberType extends FederationMember>
           extends ExecOpGenericBindJoinBase<QueryType,MemberType>
{
	public ExecOpGenericOuterBindJoinWithRequestOps( final QueryType query, final MemberType fm ) {
		super(query, fm);
	}

	@Override
	protected void _process( final IntermediateResultBlock input,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final List<SolutionMapping> unjoinableInputSMs = new ArrayList<>();
		final NullaryExecutableOp reqOp = createExecutableRequestOperator( input.getSolutionMappings(), unjoinableInputSMs );

		for ( final SolutionMapping sm : unjoinableInputSMs ) {
			sink.send(sm);
		}

		if ( reqOp != null ) {
			final MyIntermediateResultElementSink mySink = new MyIntermediateResultElementSink(sink, input);
			try {
				reqOp.execute(mySink, execCxt);
			}
			catch ( final ExecOpExecutionException e ) {
				throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
			}

			mySink.flush();
		}
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( Iterable<SolutionMapping> solMaps,
	                                                                        List<SolutionMapping> unjoinableInputSMs );


	// ------- helper classes ------

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final Iterable<SolutionMapping> inputSolutionMappings;
		protected final Set<SolutionMapping> inputSolutionMappingsWithJoinPartners = new HashSet<>();

		public MyIntermediateResultElementSink( final IntermediateResultElementSink outputSink,
		                                        final IntermediateResultBlock input ) {
			this.outputSink = outputSink;
			this.inputSolutionMappings = input.getSolutionMappings();
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			// TODO: this implementation is very inefficient
			// We need an implementation of IntermediateResultBlock that can
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
		public void flush() {
			for ( final SolutionMapping smFromInput : inputSolutionMappings ) {
				if ( ! inputSolutionMappingsWithJoinPartners.contains(smFromInput) ) {
					outputSink.send(smFromInput);
				}
			}
		}

	} // end of helper class MyIntermediateResultElementSink

}
