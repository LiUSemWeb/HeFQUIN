package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BGPRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Set;

public class RuleMergeBGPAddAndBGPReqIntoOneRequest extends AbstractRewritingRuleImpl{

    public RuleMergeBGPAddAndBGPReqIntoOneRequest( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();

        if( IdentifyPhysicalOpUsedForBGPAdd.matchBGPAdd(rootOp) ) {
            final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
            final FederationMember fm = rootLop.getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return subqueryIsBGPRequestWithSameFm( subRootOp, fm );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpBGPAdd lop = (LogicalOpBGPAdd) rootOp.getLogicalOperator();
                final BGP bgpOfBGPAdd = lop.getBGP();

                Set<TriplePattern> tpsOfReq = getTriplePatternsOfReq(plan);
                tpsOfReq.addAll(bgpOfBGPAdd.getTriplePatterns());

                final DataRetrievalRequest newReq = new BGPRequestImpl( new BGPImpl(tpsOfReq) );
                final FederationMember fm = lop.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

    static Set<TriplePattern> getTriplePatternsOfReq( PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator subRootOp = (PhysicalOperatorForLogicalOperator) plan.getSubPlan(0).getRootOperator();
        final LogicalOpRequest subRootLop = (LogicalOpRequest) subRootOp.getLogicalOperator();
        final BGP bgpOfReq = ((BGPRequest) subRootLop.getRequest()).getQueryPattern();
        final Set<TriplePattern> tpsOfReq = (Set<TriplePattern>) bgpOfReq.getTriplePatterns();
        return tpsOfReq;
    }

    protected boolean subqueryIsBGPRequestWithSameFm( final PhysicalOperator subRootOp, final FederationMember fm ) {
        if ( subRootOp instanceof PhysicalOpRequest) {
            final LogicalOpRequest subLop = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

            return ( subLop.getFederationMember() == fm ) && ( subLop.getRequest() instanceof BGPRequest );
        }
        return false;
    }

}
