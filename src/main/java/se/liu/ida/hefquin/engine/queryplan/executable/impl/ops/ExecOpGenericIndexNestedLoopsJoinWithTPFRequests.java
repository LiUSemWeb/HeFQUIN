package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public abstract class ExecOpGenericIndexNestedLoopsJoinWithTPFRequests<MemberType extends FederationMember>
                   extends ExecOpGenericIndexNestedLoopsJoin<TriplePattern,MemberType>
{
	public ExecOpGenericIndexNestedLoopsJoinWithTPFRequests( final TriplePattern query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	protected Iterable<? extends SolutionMapping> fetchSolutionMappings(
			final SolutionMapping inputSolMap,
			final ExecutionContext execCxt ) throws ExecutionException
	{
		final MaterializingIntermediateResultElementSink reqSink = new MaterializingIntermediateResultElementSink();
		final NullaryExecutableOp reqOp = createRequestOperator(inputSolMap);
		try {
			reqOp.execute(reqSink, execCxt);
		}
		catch ( final ExecutionException ex ) {
			throw new ExecutionException("An exception occurred when executing a request operator to fetch solution mappings.", ex);
		}

		return reqSink.getMaterializedIntermediateResult();
	}

	protected NullaryExecutableOp createRequestOperator( final SolutionMapping inputSolMap ) {
		final TriplePatternRequest req = createRequest(inputSolMap);
		return createRequestOperator(req);
	}

	protected TriplePatternRequest createRequest( final SolutionMapping inputSolMap ) {
		final TriplePattern tp = QueryPatternUtils.applySolMapToTriplePattern(inputSolMap, query);
		return new TriplePatternRequestImpl(tp);
	}

	protected abstract NullaryExecutableOp createRequestOperator( final TriplePatternRequest req );

}
