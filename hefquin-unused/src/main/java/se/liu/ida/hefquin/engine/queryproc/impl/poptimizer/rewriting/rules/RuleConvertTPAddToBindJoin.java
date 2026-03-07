package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationMember;

public class RuleConvertTPAddToBindJoin extends AbstractRewritingRuleImpl{

    public RuleConvertTPAddToBindJoin( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyPhysicalOpUsedForTPAdd.isIndexNLJ(rootOp) ) {
            final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator)rootOp).getLogicalOperator();
            final FederationMember fm = tpAdd.getFederationMember();
            return (fm instanceof BRTPFServer);
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpTPAdd lop = (LogicalOpTPAdd) rootOp.getLogicalOperator();
                return PhysicalPlanFactory.createPlanWithBindJoin( lop , plan.getSubPlan(0) );
            }
        };
    }

}
