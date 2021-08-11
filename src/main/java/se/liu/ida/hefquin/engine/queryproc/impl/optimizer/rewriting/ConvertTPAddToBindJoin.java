package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoin;

public abstract class ConvertTPAddToBindJoin extends ConvertTPAddToUnaryPhysicalOp{

    @Override
    public UnaryPhysicalOp definePhysicalOperator(final LogicalOpTPAdd lop ){
        final PhysicalOpBindJoin popBindJoin = new PhysicalOpBindJoin( lop );

        return popBindJoin;
    }

}
