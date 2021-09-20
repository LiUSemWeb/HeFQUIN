package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleChangeOrderOfThreeSubPlansOfJOIN2 extends AbstractRewritingRuleImpl{

    public RuleChangeOrderOfThreeSubPlansOfJOIN2( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        // root operator is JOIN
        // one of the sub plans has join as root, the other sub plan is req
        final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        if (rootOp.getLogicalOperator() instanceof LogicalOpJoin) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlanOp1).getLogicalOperator();
            final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlanOp2).getLogicalOperator();

            return (( subPlanLop1 instanceof LogicalOpJoin ) && isTriplePatternRequest(subPlanLop2))
                    ||(( subPlanLop2 instanceof LogicalOpJoin ) && isTriplePatternRequest(subPlanLop1));
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) plan.getRootOperator();

                PhysicalPlan subPlan1 = plan.getSubPlan(0);
                PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                if (( subPlanLop1 instanceof LogicalOpJoin ) && isTriplePatternRequest(subPlanLop2)) {
                    final TriplePatternRequest tpReq = (TriplePatternRequest) ((LogicalOpRequest<?, ?>) subPlanLop2).getRequest();
                    final TriplePattern tp = tpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop2).getFederationMember();
                    final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, tp );

                    final PhysicalPlan subPlanOfJoin1 = subPlan1.getSubPlan(0);
                    final PhysicalPlan subPlanOfJoin2 = subPlan1.getSubPlan(1);
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan(logicalTPAdd, subPlanOfJoin2);

                    return PhysicalPlanFactory.createPlan( rootOp, subPlanOfJoin1, newSubPlan);
                }
                else if (( subPlanLop2 instanceof LogicalOpJoin ) && isTriplePatternRequest(subPlanLop1)) {
                    final TriplePatternRequest tpReq = (TriplePatternRequest) ((LogicalOpRequest<?, ?>) subPlanLop1).getRequest();
                    final TriplePattern tp = tpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop2).getFederationMember();
                    final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, tp );

                    final PhysicalPlan subPlanOfJoin1 = subPlan2.getSubPlan(0);
                    final PhysicalPlan subPlanOfJoin2 = subPlan2.getSubPlan(1);
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan(logicalTPAdd, subPlanOfJoin1);

                    return PhysicalPlanFactory.createPlan( rootOp, newSubPlan, subPlanOfJoin2);
                }
                else  {
                    return plan;
                }
            }
        };
    }

    protected boolean isTriplePatternRequest( final LogicalOperator lop ) {
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof TriplePatternRequest;
        }
        return false;
    }

}
