package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpNaiveNestedLoopJoinTest extends TestsForJoinAlgorithms
{
	@Test
	public void joinWithEmptyInput1_SeparateInput() throws ExecutionException {
		_joinWithEmptyInput1(true, false);
	}

	@Test
	public void joinWithEmptyInput1_CombinedInput() throws ExecutionException {
		_joinWithEmptyInput1(false, false);
	}

	@Test
	public void joinWithEmptyInput2_SeparateInput() throws ExecutionException {
		_joinWithEmptyInput2(true, false);
	}

	@Test
	public void joinWithEmptyInput2_CombinedInput() throws ExecutionException {
		_joinWithEmptyInput2(false, false);
	}

	@Test
	public void joinWithEmptySolutionMapping1_SeparateInput() throws ExecutionException{
		_joinWithEmptySolutionMapping1(true, false);
	}

	@Test
	public void joinWithEmptySolutionMapping1_CombinedInput() throws ExecutionException{
		_joinWithEmptySolutionMapping1(false, false);
	}

	@Test
	public void joinWithEmptySolutionMapping2_SeparateInput() throws ExecutionException {
		_joinWithEmptySolutionMapping2(true, false);
	}

	@Test
	public void joinWithEmptySolutionMapping2_CombinedInput() throws ExecutionException {
		_joinWithEmptySolutionMapping2(false, false);
	}

	@Test
	public void joinWithOneJoinVariable_SeparateInput() throws ExecutionException {
		_joinWithOneJoinVariable(true, false);
	}

	@Test
	public void joinWithOneJoinVariable_CombinedInput() throws ExecutionException {
		_joinWithOneJoinVariable(false, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_SeparateInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(true, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_CombinedInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(false, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_SeparateInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(true, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_CombinedInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(false, false);
	}

	@Test
	public void joinWithTwoJoinVariables_SeparateInput() throws ExecutionException {
		_joinWithTwoJoinVariables(true, false);
	}

	@Test
	public void joinWithTwoJoinVariables_CombinedInput() throws ExecutionException {
		_joinWithTwoJoinVariables(false, false);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_SeparateInput() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(true, false);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_CombinedInput() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(false, false);
	}

	@Test
	public void joinWithoutJoinVariable_SeparateInput() throws ExecutionException {
		_joinWithoutJoinVariable(true, false);
	}

	@Test
	public void joinWithoutJoinVariable_CombinedInput() throws ExecutionException {
		_joinWithoutJoinVariable(false, false);
	}

	@Override
	protected BinaryExecutableOp createExecOpForTest(
			final boolean useOuterJoinSemantics,
			final ExpectedVariables... inputVars ) {
		assert useOuterJoinSemantics == false;

		return new ExecOpNaiveNestedLoopsJoin(false, null, false);
	}
}
