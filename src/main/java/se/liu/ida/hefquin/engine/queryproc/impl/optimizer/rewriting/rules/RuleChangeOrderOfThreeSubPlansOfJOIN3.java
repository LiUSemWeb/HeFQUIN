package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleChangeOrderOfThreeSubPlansOfJOIN3 extends AbstractRewritingRuleImpl{

    public RuleChangeOrderOfThreeSubPlansOfJOIN3( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        if ( rootOp.getLogicalOperator() instanceof LogicalOpJoin ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlanOp1).getLogicalOperator();
            final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlanOp2).getLogicalOperator();

            return (( subPlanLop1 instanceof LogicalOpJoin ) && isBGPRequest(subPlanLop2))
                    ||(( subPlanLop2 instanceof LogicalOpJoin ) && isBGPRequest(subPlanLop1));
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

                if (( subPlanLop1 instanceof LogicalOpJoin ) && isBGPRequest(subPlanLop2)) {
                    final BGPRequest bgpReq = (BGPRequest) ((LogicalOpRequest<?, ?>) subPlanLop2).getRequest();
                    final BGP bgp = bgpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop2).getFederationMember();
                    final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, bgp );

                    final PhysicalPlan subPlanOfJoin1 = subPlan1.getSubPlan(0);
                    final PhysicalPlan subPlanOfJoin2 = subPlan1.getSubPlan(1);
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan(logicalBGPAdd, subPlanOfJoin2);

                    return PhysicalPlanFactory.createPlan( rootOp, subPlanOfJoin1, newSubPlan);
                }
                else if (( subPlanLop2 instanceof LogicalOpJoin ) && isBGPRequest(subPlanLop1)) {
                    final BGPRequest bgpReq = (BGPRequest) ((LogicalOpRequest<?, ?>) subPlanLop1).getRequest();
                    final BGP bgp = bgpReq.getQueryPattern();

                    final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop2).getFederationMember();
                    final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, bgp );

                    final PhysicalPlan subPlanOfJoin1 = subPlan2.getSubPlan(0);
                    final PhysicalPlan subPlanOfJoin2 = subPlan2.getSubPlan(1);
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan(logicalBGPAdd, subPlanOfJoin1);

                    return PhysicalPlanFactory.createPlan( rootOp, newSubPlan, subPlanOfJoin2);
                }
                else  {
                    return plan;
                }
            }
        };
    }

    protected boolean isBGPRequest( final LogicalOperator lop ) {
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof BGPRequest;
        }
        return false;
    }

}
