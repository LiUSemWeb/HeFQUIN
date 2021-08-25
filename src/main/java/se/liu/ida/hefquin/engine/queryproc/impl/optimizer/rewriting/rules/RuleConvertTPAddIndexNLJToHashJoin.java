package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.IdentifyPhysicalOperatorOfTPAdd;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RewritingRuleBaseImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

/**
 * This is a rewriting rule that convert a TPAdd(indexNLJ) operator to Hash Join.
 */
public class RuleConvertTPAddIndexNLJToHashJoin extends RewritingRuleBaseImpl {

    public RuleConvertTPAddIndexNLJToHashJoin( final double priority ){
        super(priority);
    }

    @Override
    public boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator pop = plan.getRootOperator();
        return IdentifyPhysicalOperatorOfTPAdd.matchTPAddIndexNLJ( pop );
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] currentPath ) {
        return new RuleApplicationConvertTPAddIndexNLJToHashJoin( currentPath, this );
    }
}
