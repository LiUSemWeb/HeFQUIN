package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

public abstract class BaseForExecOpIndexNestedLoopsJoinWithTPFRequests<MemberType extends FederationMember>
           extends BaseForExecOpIndexNestedLoopsJoinWithRequestOps<TriplePattern,MemberType>
{
	protected BaseForExecOpIndexNestedLoopsJoinWithTPFRequests( final TriplePattern query,
	                                                            final MemberType fm,
	                                                            final boolean useOuterJoinSemantics,
	                                                            final boolean collectExceptions,
	                                                            final QueryPlanningInfo qpInfo ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions, qpInfo);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final SolutionMapping inputSolMap )
			throws VariableByBlankNodeSubstitutionException
	{
		final TriplePatternRequest req = createRequest(inputSolMap);
		return createRequestOperator(req);
	}

	protected TriplePatternRequest createRequest( final SolutionMapping inputSolMap )
			throws VariableByBlankNodeSubstitutionException
	{
		final TriplePattern tp = query.applySolMapToGraphPattern(inputSolMap);
		return new TriplePatternRequestImpl(tp);
	}

	protected abstract NullaryExecutableOp createRequestOperator( final TriplePatternRequest req );

}
