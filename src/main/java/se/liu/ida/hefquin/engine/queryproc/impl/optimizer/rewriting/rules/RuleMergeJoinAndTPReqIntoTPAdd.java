package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinAndTPReqIntoTPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeJoinAndTPReqIntoTPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        if (rootOp.getLogicalOperator() instanceof LogicalOpJoin) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            return isTriplePatternRequest( subPlanOp1 )
                    || isTriplePatternRequest( subPlanOp2 );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                PhysicalPlan subPlan1 = plan.getSubPlan(0);
                PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                if ( isTriplePatternRequest( subPlan1.getRootOperator() ) ) {
                    final TriplePatternRequest tpReq = (TriplePatternRequest) ((LogicalOpRequest<?, ?>) subPlanLop1).getRequest();
                    final TriplePattern tp = tpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop1).getFederationMember();
                    final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, tp );

                    return PhysicalPlanFactory.createPlan(logicalTPAdd, subPlan2);
                }
                else if ( isTriplePatternRequest( subPlan2.getRootOperator() ) ) {
                    final TriplePatternRequest tpReq = (TriplePatternRequest) ((LogicalOpRequest<?, ?>) subPlanLop2).getRequest();
                    final TriplePattern tp = tpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop2).getFederationMember();
                    final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, tp );

                    return PhysicalPlanFactory.createPlan(logicalTPAdd, subPlan1);
                }
                else  {
                    return plan;
                }
            }
        };
    }

    protected boolean isTriplePatternRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof TriplePatternRequest;
        }
        return false;
    }

}
