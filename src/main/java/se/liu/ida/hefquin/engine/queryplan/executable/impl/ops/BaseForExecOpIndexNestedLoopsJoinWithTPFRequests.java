package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public abstract class BaseForExecOpIndexNestedLoopsJoinWithTPFRequests<MemberType extends FederationMember>
           extends BaseForExecOpIndexNestedLoopsJoinWithRequestOps<TriplePattern,MemberType>
{
	protected BaseForExecOpIndexNestedLoopsJoinWithTPFRequests( final TriplePattern query,
	                                                            final MemberType fm,
	                                                            final boolean useOuterJoinSemantics,
	                                                            final boolean collectExceptions ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions);
	}

	@Override
	protected NullaryExecutableOp createExecutableRequestOperator( final SolutionMapping inputSolMap ) {
		final TriplePatternRequest req = createRequest(inputSolMap);
		return ( req == null ) ? null : createRequestOperator(req);
	}

	protected TriplePatternRequest createRequest( final SolutionMapping inputSolMap ) {
		final TriplePattern tp;
		try {
			tp = QueryPatternUtils.applySolMapToTriplePattern(inputSolMap, query);
		}
		catch ( final VariableByBlankNodeSubstitutionException e ) {
			return null;
		}

		return new TriplePatternRequestImpl(tp);
	}

	protected abstract NullaryExecutableOp createRequestOperator( final TriplePatternRequest req );

}
