package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpHashJoin2Test extends TestsForJoinAlgorithms
{
	@Test
	public void joinWithEmptyInput1_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithEmptyInput1(true, false);
	}

	@Test
	public void joinWithEmptyInput1_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithEmptyInput1(true, true);
	}

	@Test
	public void joinWithEmptyInput1_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithEmptyInput1(false, false);
	}

	@Test
	public void joinWithEmptyInput1_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithEmptyInput1(false, true);
	}

	@Test
	public void joinWithEmptyInput2_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithEmptyInput2(true, false);
	}

	@Test
	public void joinWithEmptyInput2_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithEmptyInput2(true, true);
	}

	@Test
	public void joinWithEmptyInput2_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithEmptyInput2(false, false);
	}

	@Test
	public void joinWithEmptyInput2_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithEmptyInput2(false, true);
	}

	@Test
	public void joinWithOneJoinVariable_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithOneJoinVariable(true, false);
	}

	@Test
	public void joinWithOneJoinVariable_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithOneJoinVariable(true, true);
	}

	@Test
	public void joinWithOneJoinVariable_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithOneJoinVariable(false, false);
	}

	@Test
	public void joinWithOneJoinVariable_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithOneJoinVariable(false, true);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(true, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(true, true);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(false, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_noOverlap_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_noOverlap(false, true);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(true, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(true, true);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(false, false);
	}

	@Test
	public void joinWithOneJoinVariable_withPossibleVars_overlapped_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithOneJoinVariable_withPossibleVars_overlapped(false, true);
	}

	@Test
	public void joinWithTwoJoinVariables_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithTwoJoinVariables(true, false);
	}

	@Test
	public void joinWithTwoJoinVariables_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithTwoJoinVariables(true, true);
	}

	@Test
	public void joinWithTwoJoinVariables_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithTwoJoinVariables(false, false);
	}

	@Test
	public void joinWithTwoJoinVariables_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithTwoJoinVariables(false, true);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_SeparateInput_InnerJoin() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(true, false);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_SeparateInput_OuterJoin() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(true, true);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_CombinedInput_InnerJoin() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(false, false);
	}

	@Test
	public void joinWithTwoJoinVariables_noJoinPartner_CombinedInput_OuterJoin() throws ExecutionException {
		_joinWithTwoJoinVariables_noJoinPartner(false, true);
	}


	/**
	 * Sends second input first.
	 */
	protected Iterator<SolutionMapping> runTest(
			final List<SolutionMapping> input1,
			final List<SolutionMapping> input2,
			final boolean sendAllSolMapsSeparately,
			final boolean useOuterJoinSemantics,
			final ExpectedVariables... inputVars ) throws ExecutionException
	{
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final BinaryExecutableOp op = createExecOpForTest( useOuterJoinSemantics,
		                                                   inputVars );

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
	protected BinaryExecutableOp createExecOpForTest(
			final boolean useOuterJoinSemantics,
			final ExpectedVariables... inputVars ) {
		assert inputVars.length == 2;

		return new ExecOpHashJoin2( useOuterJoinSemantics,
		                            false,            // mayReduce
		                            inputVars[0], inputVars[1],
		                            false,    // collectExceptions
		                            null );              // qpInfo
	}

}
