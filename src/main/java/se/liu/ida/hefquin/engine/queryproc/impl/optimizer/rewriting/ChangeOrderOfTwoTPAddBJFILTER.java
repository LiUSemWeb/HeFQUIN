package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;

public abstract class ChangeOrderOfTwoTPAddBJFILTER extends OrderTweakingOfTwoTPAdd {

    @Override
    public UnaryPhysicalOp defineInnerPop( final LogicalOpTPAdd lop ) {
        final PhysicalOpBindJoinWithFILTER popNewInner = new PhysicalOpBindJoinWithFILTER( lop );

        return popNewInner;
    }

    @Override
    public UnaryPhysicalOp defineRootPop( final LogicalOpTPAdd lop ) {
        final PhysicalOpBindJoinWithFILTER popNewRoot = new PhysicalOpBindJoinWithFILTER( lop );

        return popNewRoot;
    }
}
