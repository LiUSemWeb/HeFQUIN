package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public class ExecOpIndexNestedLoopsJoinBRTPF extends BaseForExecOpIndexNestedLoopsJoinWithTPFRequests<BRTPFServer>
{
	public ExecOpIndexNestedLoopsJoinBRTPF( final TriplePattern query,
	                                        final BRTPFServer fm,
	                                        final boolean useOuterJoinSemantics ) {
		super(query, fm, useOuterJoinSemantics);
	}

	@Override
	protected NullaryExecutableOp createRequestOperator(TriplePatternRequest req) {
		return new ExecOpRequestTPFatBRTPFServer(req, fm);
	}

}
