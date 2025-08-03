package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;

public class ExecOpIndexNestedLoopsJoinBRTPF extends BaseForExecOpIndexNestedLoopsJoinWithTPFRequests<BRTPFServer>
{
	public ExecOpIndexNestedLoopsJoinBRTPF( final TriplePattern query,
	                                        final BRTPFServer fm,
	                                        final boolean useOuterJoinSemantics,
	                                        final boolean collectExceptions,
	                                        final QueryPlanningInfo qpInfo ) {
		super(query, fm, useOuterJoinSemantics, collectExceptions, qpInfo);
	}

	@Override
	protected NullaryExecutableOp createRequestOperator( final TriplePatternRequest req ) {
		return new ExecOpRequestTPFatBRTPFServer(req, fm, false, null);
	}

}
