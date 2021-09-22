package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfTPReqAndBGPReqIntoOneBGPReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {
            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            if ( IdentifyPhysicalOpUsedForReq.isTriplePatternRequest( subPlanOp1 ) ) {
                final FederationMember fm = ((LogicalOpRequest) ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator()).getFederationMember();

                return ( fm instanceof SPARQLEndpoint) && IdentifyPhysicalOpUsedForReq.isBGPRequestWithFm( subPlanOp2, fm );
            }
            else if ( IdentifyPhysicalOpUsedForReq.isBGPRequest( subPlanOp1 ) ){
                final FederationMember fm = ((LogicalOpRequest) ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator()).getFederationMember();

                return ( fm instanceof SPARQLEndpoint) && IdentifyPhysicalOpUsedForReq.isTriplePatternRequestWithFm( subPlanOp2, fm );
            }
            else {
                return false;
            }
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

                final LogicalOpRequest subPlanLop1 = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOpRequest subPlanLop2 = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                final BGP newBGP = GraphPatternConstructor.createNewBGP( subPlanLop1, subPlanLop2);
                final DataRetrievalRequest newReq = new BGPRequestImpl( newBGP );

                final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop1).getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

}
