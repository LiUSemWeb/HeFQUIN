package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class ExecOpBindJoinBRTPFTest extends TestsForTPAddAlgorithms<BRTPFServer>
{
	@Test
	public void tpWithJoinOnObject() {
		_tpWithJoinOnObject();
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() {
		_tpWithJoinOnSubjectAndObject();
	}

	@Test
	public void tpWithoutJoinVariable() {
		_tpWithoutJoinVariable();
	}

	@Test
	public void tpWithEmptyInput() {
		_tpWithEmptyInput();
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() {
		_tpWithEmptySolutionMappingAsInput();
	}

	@Test
	public void tpWithEmptyResponses() {
		_tpWithEmptyResponses();
	}


	@Override
	protected BRTPFServer createFedMemberForTest( final Graph dataForMember ) {
		return new BRTPFServerForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp, final BRTPFServer fm ) {
		return new ExecOpBindJoinBRTPF(tp, fm);
	}
}
