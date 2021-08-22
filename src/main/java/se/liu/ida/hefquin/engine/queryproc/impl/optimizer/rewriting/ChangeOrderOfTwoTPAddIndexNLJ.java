package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;

public abstract class ChangeOrderOfTwoTPAddIndexNLJ extends OrderTweakingOfTwoTPAdd {

    @Override
    public UnaryPhysicalOp defineInnerPop( final LogicalOpTPAdd lop ) {
        return new PhysicalOpIndexNestedLoopsJoin( lop );
    }

    @Override
    public UnaryPhysicalOp defineRootPop( final LogicalOpTPAdd lop ) {
        return new PhysicalOpIndexNestedLoopsJoin( lop );
    }

}
