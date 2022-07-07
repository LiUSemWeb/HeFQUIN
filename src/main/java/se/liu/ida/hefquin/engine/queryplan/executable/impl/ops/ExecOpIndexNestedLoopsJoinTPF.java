package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;

public class ExecOpIndexNestedLoopsJoinTPF extends ExecOpGenericIndexNestedLoopsJoinWithTPFRequests<TPFServer>
{
	public ExecOpIndexNestedLoopsJoinTPF( final TriplePattern query, final TPFServer fm ) {
		super( query, fm );
	}

	@Override
	protected NullaryExecutableOp createRequestOperator(TriplePatternRequest req) {
		return new ExecOpRequestTPFatTPFServer(req, fm);
	}

}
