package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class RuleConvertTPAddBJFilterToHashJoin extends ConvertTPAddToHashJoin {

    @Override
    public Boolean canBeAppliedTo(PhysicalPlan pp) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();
        if( pop instanceof PhysicalOpBindJoinWithFILTER){
            if ( lop instanceof LogicalOpTPAdd ){
                return true;
            }
            return false;
        }
        return false;
    }

}
