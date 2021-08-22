package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

/**
 * This class contains methods that identifying the root physical operator of a physical plan.
 * It helps to check whether the root physical operator a physical plan can be applied to a rule.
 */
public class IdentifyPhysicalOperatorOfTPAdd {
    protected final PhysicalOperatorForLogicalOperator pop;

    public IdentifyPhysicalOperatorOfTPAdd( final PhysicalPlan pp ){
        this.pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
    }

    public boolean matchTPAdd() {
        if ( pop.getLogicalOperator() instanceof LogicalOpTPAdd ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddIndexNLJ() {
        if( matchTPAdd() && (pop instanceof PhysicalOpIndexNestedLoopsJoin) ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBJFILTER() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoinWithFILTER) ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBJUNION() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoinWithUNION) ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBJVALUES() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoinWithVALUES) ){
            return true;
        }
        return false;
    }

    public boolean matchTPAddBindJoin() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoin) ){
            return true;
        }
        return false;
    }

}
