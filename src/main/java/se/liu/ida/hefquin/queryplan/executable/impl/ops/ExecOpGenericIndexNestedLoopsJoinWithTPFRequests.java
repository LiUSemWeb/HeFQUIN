package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.query.TriplePattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;
import se.liu.ida.hefquin.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithTPFRequests<MemberType extends FederationMember>
                   extends ExecOpGenericIndexNestedLoopsJoin<TriplePattern,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithTPFRequests( final TriplePattern query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected Iterator<? extends SolutionMapping> fetchSolutionMappings(
			final SolutionMapping inputSolMap,
			final ExecutionContext execCxt )
	{
		final MaterializingIntermediateResultElementSink reqSink = new MaterializingIntermediateResultElementSink();
		createRequestOperator(inputSolMap).execute(reqSink, execCxt);

		return reqSink.getMaterializedIntermediateResult().iterator();
	}

	protected NullaryExecutableOp createRequestOperator( final SolutionMapping inputSolMap ) {
		final TriplePatternRequest req = createRequest(inputSolMap);
		return createRequestOperator(req);
	}

	protected TriplePatternRequest createRequest( final SolutionMapping inputSolMap ) {
		final TriplePattern tp = JenaBasedQueryPatternUtils.applySolMapToTriplePattern(inputSolMap, query);
		return new TriplePatternRequestImpl(tp);
	}

	protected abstract NullaryExecutableOp createRequestOperator( final TriplePatternRequest req );

}
