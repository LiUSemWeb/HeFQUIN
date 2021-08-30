package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Set;

public class GenericRuleMergeTPAddAndBGPAddIntoBGPAdd extends AbstractRewritingRuleImpl{

    public GenericRuleMergeTPAddAndBGPAddIntoBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();

        if( IdentifyPhysicalOpUsedForTPAdd.matchTPAdd(rootOp) ) {
            final LogicalOpTPAdd rootLop = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
            final FederationMember fm = rootLop.getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return subqueryIsBGPAddWithSameFm( subRootOp, fm );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication(PhysicalPlan[] pathToTargetPlan) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final LogicalOpTPAdd rootOp = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
                final TriplePattern tp = rootOp.getTP();

                final LogicalOpBGPAdd subRootOp = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator)plan.getSubPlan(0).getRootOperator()).getLogicalOperator();
                final BGP bgp = subRootOp.getBGP();
                final Set<TriplePattern> tps = (Set<TriplePattern>) bgp.getTriplePatterns();
                tps.add(tp);

                final FederationMember fm = rootOp.getFederationMember();
                final PhysicalOperator newRootOp = new PhysicalOpIndexNestedLoopsJoin( new LogicalOpBGPAdd( fm, new BGPImpl(tps) ) );

                return PhysicalPlanFactory.createPlan( newRootOp, plan.getSubPlan(0).getSubPlan(0) );
            }
        };
    }

    static boolean subqueryIsBGPAddWithSameFm( final PhysicalOperator subRootOp, final FederationMember fm) {
        if ( IdentifyPhysicalOpUsedForBGPAdd.matchBGPAdd(subRootOp) ) {
            final LogicalOpBGPAdd subLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

            return ( subLop.getFederationMember() == fm );
        }
        return false;
    }

}
