package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class RuleConvertTPAddIndexNLJToBJFilter implements Rule {

    @Override
    public Boolean canBeAppliedTo( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();
        if( pop instanceof PhysicalOpIndexNestedLoopsJoin){
            if ( lop instanceof LogicalOpTPAdd ){
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public PhysicalPlan applyTo(PhysicalPlan pp) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();

        final PhysicalOpBindJoinWithFILTER popJoin = new PhysicalOpBindJoinWithFILTER( lop );
        final PhysicalPlan ppNew = new PhysicalPlanWithUnaryRootImpl( popJoin, pp.getSubPlan(0));

        return ppNew;
    }

    @Override
    public Double getPriority() {
        return 0.2;
    }
}
