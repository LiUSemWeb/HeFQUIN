package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

public class ExecOpNaiveNestedLoopJoinTest extends TestsForJoinAlgorithms{

    @Test
    public void joinWithOneJoinVariable() {
        _joinWithOneJoinVariable();
    }

    @Test
    public void joinWithTwoJoinVariables() { _joinWithTwoJoinVariables(); }

    @Test
    public void joinWithoutJoinVariable() { _joinWithoutJoinVariable(); }

    @Test
    public void joinWithEmptyInput1() { _joinWithEmptyInput1(); }

    @Test
    public void joinWithEmptyInput2() { _joinWithEmptyInput2(); }

    @Override
    protected BinaryExecutableOp createExecOpForTest() {
        return new ExecOpNaiveNestedLoopsJoin( );
    }
}
