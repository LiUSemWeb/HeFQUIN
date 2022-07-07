package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpBindJoinBRTPFTest extends TestsForTPAddAlgorithms<BRTPFServer>
{
	@Test
	public void tpWithJoinOnObject() throws ExecutionException {
		_tpWithJoinOnObject();
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject();
	}

	@Test
	public void tpWithoutJoinVariable() throws ExecutionException {
		_tpWithoutJoinVariable();
	}

	@Test
	public void tpWithEmptyInput() throws ExecutionException {
		_tpWithEmptyInput();
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput();
	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses();
	}


	@Override
	protected BRTPFServer createFedMemberForTest( final Graph dataForMember ) {
		return new BRTPFServerForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest(final TriplePattern tp, final BRTPFServer fm,
													final ExpectedVariables expectedVariables) {
		return new ExecOpBindJoinBRTPF(tp, fm);
	}
}
