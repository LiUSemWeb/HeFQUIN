package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithRequestOps<
                                                    QueryType extends Query,
                                                    MemberType extends FederationMember>
              extends ExecOpGenericIndexNestedLoopsJoinBase<QueryType,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithRequestOps( final QueryType query, final MemberType fm ) {
		super(query, fm);
	}

	@Override
	protected void process(
			final SolutionMapping sm,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt) throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator(sm);
		final IntermediateResultElementSink mySink = new MyIntermediateResultElementSink(sink, sm);
		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
			throw new ExecOpExecutionException("Executing a request operator used by this index nested loops join caused an exception.", e, this);
		}
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( SolutionMapping sm );


	// ------- helper classes ------

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink outputSink;
		protected final SolutionMapping smFromInput;

		public MyIntermediateResultElementSink( final IntermediateResultElementSink outputSink,
		                                        final SolutionMapping smFromInput ) {
			this.outputSink = outputSink;
			this.smFromInput = smFromInput;
		}

		@Override
		public void send( final SolutionMapping smFromRequest ) {
			outputSink.send( SolutionMappingUtils.merge(smFromInput,smFromRequest) );
		}
    } // end of helper class MyIntermediateResultElementSink

}
