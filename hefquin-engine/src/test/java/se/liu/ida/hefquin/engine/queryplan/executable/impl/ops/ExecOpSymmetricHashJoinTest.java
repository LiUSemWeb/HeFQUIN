package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpSymmetricHashJoinTest extends TestsForInnerJoinAlgorithms
{
    @Test
    public void joinWithOneJoinVariable() throws ExecutionException { _joinWithOneJoinVariable(); }

    @Test
    public void joinWithTwoJoinVariables() throws ExecutionException { _joinWithTwoJoinVariables(); }

    @Test
    public void joinWithTwoJoinVariables_noJoinPartner() throws ExecutionException { _joinWithTwoJoinVariables_noJoinPartner(); }

    @Test
    public void joinWithOneJoinVariable_withPossibleVars_noOverlap() throws ExecutionException { _joinWithOneJoinVariable_withPossibleVars_noOverlap(); }

    @Test
    public void joinWithOneJoinVariable_withPossibleVars_overlapped() throws ExecutionException { _joinWithOneJoinVariable_withPossibleVars_overlapped(); }

    @Override
    protected BinaryExecutableOp createExecOpForTest(final ExpectedVariables... inputVars) {
        assert inputVars.length == 2;

        return new ExecOpSymmetricHashJoin( inputVars[0], inputVars[1], false);
    }
}
