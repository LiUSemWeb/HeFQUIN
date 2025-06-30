package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.TPFServer;

public class ExecOpIndexNestedLoopsJoinTPFTest extends TestsForTPAddAlgorithms<TPFServer>
{
	@Test
	public void tpWithJoinOnObject_InnerJoin() throws ExecutionException {
		_tpWithJoinOnObject(false);
	}
	@Test
	public void tpWithJoinOnObject_OuterJoin() throws ExecutionException {
		_tpWithJoinOnObject(true);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_InnerJoin() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(false);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_OuterJoin() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(true);
	}

	@Test
	public void tpWithoutJoinVariable_InnerJoin() throws ExecutionException {
		_tpWithoutJoinVariable(false);
	}

	@Test
	public void tpWithoutJoinVariable_OuterJoin() throws ExecutionException {
		_tpWithoutJoinVariable(true);
	}

	@Test
	public void tpWithEmptyInput_InnerJoin() throws ExecutionException {
		_tpWithEmptyInput(false);
	}

	@Test
	public void tpWithEmptyInput_OuterJoin() throws ExecutionException {
		_tpWithEmptyInput(true);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_InnerJoin() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(false);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_OuterJoin() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(true);
	}

	@Test
	public void tpWithEmptyResponses_InnerJoin() throws ExecutionException {
		_tpWithEmptyResponses(false);
	}

	@Test
	public void tpWithEmptyResponses_OuterJoin() throws ExecutionException {
		_tpWithEmptyResponses(true);
	}

	@Test
	public void tpWithIllegalBNodeJoin_InnerJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(false);
	}

	@Test
	public void tpWithIllegalBNodeJoin_OuterJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(true);
	}


	@Override
	protected TPFServer createFedMemberForTest( final Graph dataForMember ) {
		return new TPFServerForTest(dataForMember);
	}

	@Override
	protected ExecutorService getExecutorServiceForTest() {
		return null;
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp,
	                                                 final TPFServer fm,
	                                                 final ExpectedVariables expectedVariables,
	                                                 final boolean useOuterJoinSemantics ) {
		return new ExecOpIndexNestedLoopsJoinTPF(tp, fm, useOuterJoinSemantics, false);
	}
}
