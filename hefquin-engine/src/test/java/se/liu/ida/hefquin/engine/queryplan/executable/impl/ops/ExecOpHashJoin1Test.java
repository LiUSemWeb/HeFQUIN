package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpHashJoin1Test extends TestsForJoinAlgorithms
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

	@Override
	protected BinaryExecutableOp createExecOpForTest(
			final boolean useOuterJoinSemantics,
			final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;
		assert useOuterJoinSemantics == false;

		return new ExecOpHashJoin1( inputVars[0], inputVars[1], false, null, false );
	}

}
