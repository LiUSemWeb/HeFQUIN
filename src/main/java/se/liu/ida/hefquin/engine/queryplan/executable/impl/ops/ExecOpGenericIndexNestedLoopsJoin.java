package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.jenaimpl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericIndexNestedLoopsJoin<QueryType extends Query, MemberType extends FederationMember>
                   implements UnaryExecutableOp
{
	protected final QueryType query;
	protected final MemberType fm;

	public ExecOpGenericIndexNestedLoopsJoin( final QueryType query, final MemberType fm ) {
		assert query != null;
		assert fm != null;

		this.query = query;
		this.fm = fm;
	}

	@Override
	public int preferredInputBlockSize() {
		// Since this algorithm processes the input solution mappings
		// sequentially (one at a time), and input block size of 1 may
		// reduce the response time of the overall execution process.
		return 1;  
	}

	@Override
	public void process(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt)
	{
		for ( final SolutionMapping sm : input.getSolutionMappings() ) {
			process( sm, sink, execCxt );
		}
	}

	@Override
	public void concludeExecution(
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		// nothing to be done here
	}

	protected void process(
			final SolutionMapping sm,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt)
	{
		for ( final SolutionMapping fetchedSM : fetchSolutionMappings(sm,execCxt) ) {
			final SolutionMapping out = SolutionMappingUtils.merge( sm, fetchedSM );
			sink.send(out);
		}
	}

	protected abstract Iterable<? extends SolutionMapping> fetchSolutionMappings(
			final SolutionMapping sm,
			final ExecutionContext execCxt );

}
