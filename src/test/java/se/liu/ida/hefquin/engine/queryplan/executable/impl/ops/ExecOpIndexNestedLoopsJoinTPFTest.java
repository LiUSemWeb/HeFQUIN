package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class ExecOpIndexNestedLoopsJoinTPFTest extends TestsForTPAddAlgorithms<TPFServer>
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
	public void tpWithEmptyResponses() {
		_tpWithEmptyResponses();
	}


	@Override
	protected TPFServer createFedMemberForTest( final Graph dataForMember ) {
		return new TPFServerForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp, final TPFServer fm ) {
		return new ExecOpIndexNestedLoopsJoinTPF(tp, fm);
	}
}
