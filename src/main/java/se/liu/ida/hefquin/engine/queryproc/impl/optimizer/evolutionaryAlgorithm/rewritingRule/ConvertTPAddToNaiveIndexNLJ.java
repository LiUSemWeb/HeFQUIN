package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpNaiveNestedLoopsJoin;

public abstract class ConvertTPAddToNaiveIndexNLJ extends ConvertTPAddToBinaryOperator {

    @Override
    public BinaryPhysicalOp definePhysicalOperator(){
        final PhysicalOpNaiveNestedLoopsJoin popJoin = new PhysicalOpNaiveNestedLoopsJoin( new LogicalOpJoin() );

        return popJoin;
    }

}
