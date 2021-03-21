package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

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
	public void process(
			final IntermediateResultBlock input,
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt)
	{
		final Iterator<SolutionMapping> it = input.iterator();
		while ( it.hasNext() ) {
			process( it.next(), sink, execCxt );
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
		final Iterator<SolutionMapping> it = fetchSolutionMappings(sm, execCxt);
		while ( it.hasNext() ) {
			final SolutionMapping out = JenaBasedSolutionMappingUtils.merge( sm, it.next() );
			sink.send(out);
		}
	}

	protected abstract Iterator<SolutionMapping> fetchSolutionMappings(
			final SolutionMapping sm,
			final ExecutionContext execCxt );

}
