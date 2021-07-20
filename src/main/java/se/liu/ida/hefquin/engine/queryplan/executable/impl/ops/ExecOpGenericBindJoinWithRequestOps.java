package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Abstract base class to implements bind joins by using request operators.
 */
public abstract class ExecOpGenericBindJoinWithRequestOps<QueryType extends Query,
                                                          MemberType extends FederationMember>
           extends ExecOpGenericBindJoinBase<QueryType,MemberType>
{
	public ExecOpGenericBindJoinWithRequestOps( final QueryType query, final MemberType fm ) {
		super(query, fm);
	}

	@Override
	public void process( final IntermediateResultBlock input,
	                     final IntermediateResultElementSink sink,
	                     final ExecutionContext execCxt)
			throws ExecOpExecutionException
	{
		final NullaryExecutableOp reqOp = createExecutableRequestOperator( input.getSolutionMappings() );
		final IntermediateResultElementSink mySink = new MyIntermediateResultElementSink(sink, input);
		try {
			reqOp.execute(mySink, execCxt);
		}
		catch ( final ExecOpExecutionException e ) {
			throw new ExecOpExecutionException("Executing a request operator used by this bind join caused an exception.", e, this);
		}
	}

	protected abstract NullaryExecutableOp createExecutableRequestOperator( Iterable<SolutionMapping> solMaps );


	// ------- helper classes ------

	protected static class MyIntermediateResultElementSink implements IntermediateResultElementSink
	{
		protected final IntermediateResultElementSink parentSink;
		protected final Iterable<SolutionMapping> inputSolutionMappings;

		public MyIntermediateResultElementSink( final IntermediateResultElementSink parentSink,
		                                        final IntermediateResultBlock input ) {
			this.parentSink = parentSink;
			this.inputSolutionMappings = input.getSolutionMappings();
		}

		@Override
		public void send( final SolutionMapping fetchedSM ) {
			for ( final SolutionMapping inputSM : inputSolutionMappings ) {
				if ( SolutionMappingUtils.compatible(inputSM, fetchedSM) ) {
					final SolutionMapping mergedSM = SolutionMappingUtils.merge(inputSM, fetchedSM);
					parentSink.send(mergedSM);
				}
			}
		}
    } // end of helper class MyIntermediateResultElementSink

}
