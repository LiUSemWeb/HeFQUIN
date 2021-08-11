package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;

public abstract class ConvertTPAddToBindJoinWithFILTER extends ConvertTPAddToUnaryPhysicalOp{

    @Override
    public UnaryPhysicalOp definePhysicalOperator( final LogicalOpTPAdd lop ){
        final PhysicalOpBindJoinWithFILTER popBindJoinWithFILTER = new PhysicalOpBindJoinWithFILTER( lop );

        return popBindJoinWithFILTER;
    }

}
