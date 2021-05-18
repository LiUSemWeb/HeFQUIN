package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;

public class ExecOpNaiveNestedLoopJoinTest extends TestsForJoinAlgorithms{

    @Test
    public void joinWithOneJoinVariable() { _joinWithOneJoinVariable(); }

    @Test
    public void joinWithTwoJoinVariables() { _joinWithTwoJoinVariables(); }

    @Test
    public void joinWithTwoJoinVariables_noJoinPartner() { _joinWithTwoJoinVariables_noJoinPartner(); }

    @Test
    public void joinWithoutJoinVariable() { _joinWithoutJoinVariable(); }

    @Test
    public void joinWithEmptyInput1() { _joinWithEmptyInput1(); }

    @Test
    public void joinWithEmptyInput2() { _joinWithEmptyInput2(); }

    @Test
    public void joinWithEmptySolutionMapping1() { _joinWithEmptySolutionMapping1(); }

    @Test
    public void joinWithEmptySolutionMapping2() { _joinWithEmptySolutionMapping2(); }

    @Override
    protected BinaryExecutableOp createExecOpForTest( final ExpectedVariables... inputVars ) {
        return new ExecOpNaiveNestedLoopsJoin( );
    }
}
