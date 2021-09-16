package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleConvertBGPAddToBJVALUES extends AbstractRewritingRuleImpl{

    public RuleConvertBGPAddToBJVALUES( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOpUsedForBGPAdd.isIndexNLJ(rootOp)
                || IdentifyPhysicalOpUsedForBGPAdd.isBindJoinFILTER(rootOp)
                || IdentifyPhysicalOpUsedForBGPAdd.isBindJoinUNION(rootOp);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpBGPAdd lop = (LogicalOpBGPAdd) rootOp.getLogicalOperator();
                return PhysicalPlanFactory.createPlanWithBindJoinVALUES( lop , plan.getSubPlan(0) );
            }
        };
    }

}
