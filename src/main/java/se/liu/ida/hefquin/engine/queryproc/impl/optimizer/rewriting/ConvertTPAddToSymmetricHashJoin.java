package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;

public abstract class ConvertTPAddToSymmetricHashJoin extends ConvertTPAddToBinaryOperator{

    @Override
    public BinaryPhysicalOp definePhysicalOperator(){
        final PhysicalOpSymmetricHashJoin popJoin = new PhysicalOpSymmetricHashJoin( new LogicalOpJoin() );

        return popJoin;
    }

}
