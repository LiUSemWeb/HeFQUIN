package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class RuleMergeBGPAddOfGraphPatternReqIntoOneRequest extends AbstractRewritingRuleImpl{

    public RuleMergeBGPAddOfGraphPatternReqIntoOneRequest( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        final LogicalOperator rootLop = ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();

        if( rootLop instanceof LogicalOpBGPAdd ) {
            final FederationMember fm = ((LogicalOpBGPAdd)rootLop).getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return IdentifyTypeOfRequestUsedForReq.isGraphPatternReqWithFm( subRootOp, fm );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) rootOp.getLogicalOperator();

                final PhysicalOperatorForLogicalOperator subRootOp = (PhysicalOperatorForLogicalOperator) plan.getSubPlan(0).getRootOperator();
                final LogicalOpRequest<?, ?> subRootLop = (LogicalOpRequest<?, ?>) subRootOp.getLogicalOperator();

                final SPARQLGraphPattern newGraphPattern = LogicalOpUtils.createNewGraphPatternWithAND(rootLop, subRootLop);
                final SPARQLRequestImpl newReq = new SPARQLRequestImpl( newGraphPattern );
                final FederationMember fm = rootLop.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

}
