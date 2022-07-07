package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public class ExecOpIndexNestedLoopsJoinBRTPF extends ExecOpGenericIndexNestedLoopsJoinWithTPFRequests<BRTPFServer>
{
	public ExecOpIndexNestedLoopsJoinBRTPF( final TriplePattern query, final BRTPFServer fm ) {
		super( query, fm );
	}

	@Override
	protected NullaryExecutableOp createRequestOperator(TriplePatternRequest req) {
		return new ExecOpRequestTPFatBRTPFServer(req, fm);
	}

}
