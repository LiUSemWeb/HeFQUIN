package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class ExecOpSymmetricHashJoinTest extends TestsForJoinAlgorithms{

    @Test
    public void joinWithOneJoinVariable() { _joinWithOneJoinVariable(); }

    @Test
    public void joinWithTwoJoinVariables() { _joinWithTwoJoinVariables(); }

    @Test
    public void joinWithTwoJoinVariables_noJoinPartner() { _joinWithTwoJoinVariables_noJoinPartner(); }

    @Test
    public void joinWithOneJoinVariable_withPossibleVars_noOverlap() { _joinWithOneJoinVariable_withPossibleVars_noOverlap(); }

    @Test
    public void joinWithOneJoinVariable_withPossibleVars_overlapped() { _joinWithOneJoinVariable_withPossibleVars_overlapped(); }

    @Override
    protected BinaryExecutableOp createExecOpForTest(final ExpectedVariables... inputVars) {
        return new ExecOpSymmetricHashJoin(inputVars);
    }
}
