package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashJoin;

/**
 * This is a class for rule applications that convert any TPAdd operator to Hash join algorithm.
 */
public abstract class ConvertTPAddToHashJoin extends ConvertTPAddToBinaryOperator {

    @Override
    public BinaryPhysicalOp definePhysicalOperator(){
        return new PhysicalOpHashJoin( new LogicalOpJoin() );
    }

}
