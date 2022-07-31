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
		_tpWithJoinOnObject(false);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(false);
	}

	@Test
	public void tpWithoutJoinVariable() throws ExecutionException {
		_tpWithoutJoinVariable(false);
	}

	@Test
	public void tpWithEmptyInput() throws ExecutionException {
		_tpWithEmptyInput(false);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(false);
	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses(false);
	}


	@Override
	protected BRTPFServer createFedMemberForTest( final Graph dataForMember ) {
		return new BRTPFServerForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp,
	                                                 final BRTPFServer fm,
	                                                 final ExpectedVariables expectedVariables,
	                                                 final boolean useOuterJoinSemantics ) {
		if ( useOuterJoinSemantics )
			throw new UnsupportedOperationException();

		return new ExecOpBindJoinBRTPF(tp, fm);
	}
}
