package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.BinaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpNaiveNestedLoopJoinTest extends TestsForInnerJoinAlgorithms
{
    @Test
    public void joinWithOneJoinVariable() throws ExecutionException { _joinWithOneJoinVariable(); }

    @Test
    public void joinWithTwoJoinVariables() throws ExecutionException { _joinWithTwoJoinVariables(); }

    @Test
    public void joinWithTwoJoinVariables_noJoinPartner() throws ExecutionException { _joinWithTwoJoinVariables_noJoinPartner(); }

    @Test
    public void joinWithoutJoinVariable() throws ExecutionException { _joinWithoutJoinVariable(); }

    @Test
    public void joinWithEmptyInput1() throws ExecutionException { _joinWithEmptyInput1(); }

    @Test
    public void joinWithEmptyInput2() throws ExecutionException { _joinWithEmptyInput2(); }

    @Test
    public void joinWithEmptySolutionMapping1() throws ExecutionException{ _joinWithEmptySolutionMapping1(); }

    @Test
    public void joinWithEmptySolutionMapping2() throws ExecutionException { _joinWithEmptySolutionMapping2(); }

    @Override
    protected BinaryExecutableOp createExecOpForTest( final ExpectedVariables... inputVars ) {
        return new ExecOpNaiveNestedLoopsJoin(false);
    }
}
