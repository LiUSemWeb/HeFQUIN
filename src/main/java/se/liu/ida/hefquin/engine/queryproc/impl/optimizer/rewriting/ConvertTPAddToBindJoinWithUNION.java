package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;

public abstract class ConvertTPAddToBindJoinWithUNION extends ConvertTPAddToUnaryPhysicalOp{

    @Override
    public UnaryPhysicalOp definePhysicalOperator(final LogicalOpTPAdd lop ){
        final PhysicalOpBindJoinWithUNION popBindJoinWithUNION = new PhysicalOpBindJoinWithUNION( lop );

        return popBindJoinWithUNION;
    }

}
