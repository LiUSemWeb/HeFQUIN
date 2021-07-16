package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoin;

public abstract class ChangeOrderOfTwoTPAddBindJoin extends OrderTweakingOfTwoTPAdd {

    @Override
    public UnaryPhysicalOp defineInnerPop( final LogicalOpTPAdd lop ) {
        final PhysicalOpBindJoin popNewInner = new PhysicalOpBindJoin( lop );

        return popNewInner;
    }

    @Override
    public UnaryPhysicalOp defineRootPop( final LogicalOpTPAdd lop ) {
        final PhysicalOpBindJoin popNewRoot = new PhysicalOpBindJoin( lop );

        return popNewRoot;
    }

}
