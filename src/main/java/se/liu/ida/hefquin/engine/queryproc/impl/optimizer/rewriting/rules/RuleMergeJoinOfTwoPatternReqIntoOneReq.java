package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfTwoPatternReqIntoOneReq extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfTwoPatternReqIntoOneReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.matchJoin(rootOp) ) {

            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            if ( IdentifyPhysicalOpUsedForReq.isGraphPatternRequest( subPlanOp1 ) ) {
                final FederationMember fm = ((LogicalOpRequest) ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator()).getFederationMember();

                return IdentifyPhysicalOpUsedForReq.isGraphPatternReqWithFm( subPlanOp2, fm );
            }
            return false;
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalPlan subPlan1 = plan.getSubPlan(0);
                final PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                final SPARQLGraphPattern newGraphPattern = GraphPatternConstructor.createNewGraphPatternWithAND( subPlanLop1, subPlanLop2 );
                final SPARQLRequestImpl newReq = new SPARQLRequestImpl( newGraphPattern );

                final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop1).getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

}
