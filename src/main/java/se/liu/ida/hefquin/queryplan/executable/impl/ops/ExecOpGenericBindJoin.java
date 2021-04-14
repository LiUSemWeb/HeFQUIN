package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMappingUtils;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

import java.util.Iterator;
import java.util.Set;

public abstract class ExecOpGenericBindJoin<QueryType extends Query, MemberType extends FederationMember>
		implements UnaryExecutableOp
{
	protected final QueryType query;
	protected final MemberType fm;

	public ExecOpGenericBindJoin(final QueryType query, final MemberType fm ) {
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
		final Iterator<SolutionMapping> it = input.iterator();
		Set<SolutionMapping> solMaps = null;
		while ( it.hasNext() ) {
			solMaps.add( it.next());
		}
		final Iterator<? extends SolutionMapping> out = fetchSolutionMappings(solMaps, execCxt);
		while ( out.hasNext() ) {
			sink.send(out.next());
		}
	}

	@Override
	public void concludeExecution(
			final IntermediateResultElementSink sink,
			final ExecutionContext execCxt )
	{
		// nothing to be done here
	}

	protected abstract Iterator<? extends SolutionMapping> fetchSolutionMappings(
			final Set<SolutionMapping> solMaps,
			final ExecutionContext execCxt );
}
