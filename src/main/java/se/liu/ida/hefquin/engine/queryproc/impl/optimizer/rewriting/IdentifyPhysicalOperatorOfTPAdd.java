package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class IdentifyPhysicalOperatorOfTPAdd {
    protected final PhysicalOperatorForLogicalOperator pop;

    public IdentifyPhysicalOperatorOfTPAdd( final PhysicalPlan pp ){
        this.pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
    }

    public boolean matchTPAdd() {
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();
        if ( lop instanceof LogicalOpTPAdd ){
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
