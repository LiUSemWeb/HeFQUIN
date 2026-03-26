package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpHashJoin2Test extends TestsForInnerJoinAlgorithms
{
	@Test
	public void joinWithEmptyInput1_SeparateInput() throws ExecutionException {
		_joinWithEmptyInput1(true);
	}

	@Test
	public void joinWithEmptyInput1_CombinedInput() throws ExecutionException {
		_joinWithEmptyInput1(false);
	}

	@Test
	public void joinWithEmptyInput2_SeparateInput() throws ExecutionException {
		_joinWithEmptyInput2(true);
	}

	@Test
	public void joinWithEmptyInput2_CombinedInput() throws ExecutionException {
		_joinWithEmptyInput2(false);
	}

	@Test
	public void joinWithOneJoinVariable_SeparateInput() throws ExecutionException {
		_joinWithOneJoinVariable(true);
	}

	@Test
	public void joinWithOneJoinVariable_CombinedInput() throws ExecutionException {
		_joinWithOneJoinVariable(false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_SeparateInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(true);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_CombinedInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_SeparateInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(true);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_CombinedInput() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(false);
	}

	@Test
	public void joinWithTwoJoinVariables_SeparateInput() throws ExecutionException {
		_joinWithTwoJoinVariables(true);
	}

	@Test
	public void joinWithTwoJoinVariables_CombinedInput() throws ExecutionException {
		_joinWithTwoJoinVariables(false);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_SeparateInput() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(true);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_CombinedInput() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(false);
	}


	/**
	 * Sends second input first.
	 */
	protected Iterator<SolutionMapping> runTest(
			final List<SolutionMapping> input1,
			final List<SolutionMapping> input2,
			final boolean sendAllSolMapsSeparately,
			final ExpectedVariables... inputVars ) throws ExecutionException
	{
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final BinaryExecutableOp op = createExecOpForTest(inputVars);

		if ( sendAllSolMapsSeparately == true ) {
			for ( final SolutionMapping sm : input2 ) {
				op.processInputFromChild2(sm, sink, null);
			}
		}
		else {
			op.processInputFromChild2(input2, sink, null);
		}

		op.wrapUpForChild2(sink, null);

		if ( sendAllSolMapsSeparately == true ) {
			for ( final SolutionMapping sm : input1 ) {
				op.processInputFromChild1(sm, sink, null);
			}
		}
		else {
			op.processInputFromChild1(input1, sink, null);
		}

		op.wrapUpForChild1(sink, null);

		return sink.getCollectedSolutionMappings().iterator();
	}

	@Override
	protected BinaryExecutableOp createExecOpForTest( final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashJoin2( inputVars[0], inputVars[1], false, null );
	}

}
