package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashJoin;

public abstract class ConvertTPAddToHashJoin extends ConvertTPAddToBinaryOperator {

    @Override
    public BinaryPhysicalOp definePhysicalOperator(){
        final PhysicalOpHashJoin popJoin = new PhysicalOpHashJoin( new LogicalOpJoin() );

        return popJoin;
    }

}
