package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.impl.req.BGPRequestImpl;

public abstract class GenericRuleMergeJoinOfTwoReqIntoOneBGPReq extends AbstractRewritingRuleImpl{

    public GenericRuleMergeJoinOfTwoReqIntoOneBGPReq( final double priority ) {
        super(priority);
    }

    @Override
    protected RuleApplication createRuleApplication(final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan(final PhysicalPlan plan ) {
                final PhysicalPlan subPlan1 = plan.getSubPlan(0);
                final PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOpRequest<?, ?> subPlanLop1 = (LogicalOpRequest<?, ?>) ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOpRequest<?, ?> subPlanLop2 = (LogicalOpRequest<?, ?>) ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                final BGP newBGP = LogicalOpUtils.createNewBGP( subPlanLop1, subPlanLop2);
                final DataRetrievalRequest newReq = new BGPRequestImpl( newBGP );

                final FederationMember fm = subPlanLop1.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

}
