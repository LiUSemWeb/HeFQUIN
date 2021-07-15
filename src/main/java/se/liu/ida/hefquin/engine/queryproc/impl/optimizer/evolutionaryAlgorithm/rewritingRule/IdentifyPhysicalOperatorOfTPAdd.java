package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class IdentifyPhysicalOperatorOfTPAdd {
    protected final PhysicalOperatorForLogicalOperator pop;

    IdentifyPhysicalOperatorOfTPAdd( final PhysicalPlan pp ){
        this.pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
    }

    public Boolean matchTPAdd() {
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();
        if ( lop instanceof LogicalOpTPAdd ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddIndexNLJ() {
        if( matchTPAdd() && (pop instanceof PhysicalOpIndexNestedLoopsJoin) ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJFilter() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoinWithFILTER) ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJUNION() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoinWithUNION) ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJVALUES() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoinWithVALUES) ){
            return true;
        }
        return false;
    }

    public Boolean matchTPAddBJ() {
        if( matchTPAdd() && ( pop instanceof PhysicalOpBindJoin) ){
            return true;
        }
        return false;
    }

}
