package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Set;

public class RuleMergeTwoBGPAddIntoOneBGPAdd extends AbstractRewritingRuleImpl{

    public RuleMergeTwoBGPAddIntoOneBGPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();

        if( IdentifyPhysicalOpUsedForBGPAdd.matchBGPAdd(rootOp) ) {
            final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
            final FederationMember fm = rootLop.getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return subqueryIsBGPAddWithSameFm( subRootOp, fm );
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();

                final PhysicalOperator subRootOp =  plan.getSubPlan(0).getRootOperator();
                final LogicalOpBGPAdd subRootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

                final BGP newBGP = createNewBGP(rootLop, subRootLop);
                final FederationMember fm = rootLop.getFederationMember();
                final LogicalOpBGPAdd logicalBGPAdd = new LogicalOpBGPAdd( fm, newBGP );
                final PhysicalOperator newRootOp = determinePhysicalOpOfRoot( rootOp, logicalBGPAdd );

                return PhysicalPlanFactory.createPlan( newRootOp, plan.getSubPlan(0).getSubPlan(0) );
            }
        };
    }

    protected BGP createNewBGP( final LogicalOpBGPAdd lopBGPAddOut, final LogicalOpBGPAdd lopBGPAddIn ) {
        final Set<TriplePattern> tpsOfBGPAddOut = (Set<TriplePattern>) lopBGPAddOut.getBGP().getTriplePatterns();

        final Set<TriplePattern> tpsOfBGPAddIn = (Set<TriplePattern>) lopBGPAddIn.getBGP().getTriplePatterns();
        tpsOfBGPAddOut.addAll(tpsOfBGPAddIn);

        return new BGPImpl(tpsOfBGPAddOut);
    }

    protected boolean subqueryIsBGPAddWithSameFm( final PhysicalOperator subRootOp, final FederationMember fm) {
        if ( IdentifyPhysicalOpUsedForBGPAdd.matchBGPAdd(subRootOp) ) {
            final LogicalOpBGPAdd subLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

            return ( subLop.getFederationMember() == fm );
        }
        return false;
    }

    protected PhysicalOperator determinePhysicalOpOfRoot( final PhysicalOperator subRootOp, final LogicalOpBGPAdd logicalOpBGPAdd) {

        if ( subRootOp instanceof PhysicalOpIndexNestedLoopsJoin) {
            return new PhysicalOpIndexNestedLoopsJoin( logicalOpBGPAdd );
        }
        else if ( subRootOp instanceof PhysicalOpBindJoinWithFILTER) {
            return new PhysicalOpBindJoinWithFILTER( logicalOpBGPAdd );
        }
        else if ( subRootOp instanceof PhysicalOpBindJoinWithUNION) {
            return new PhysicalOpBindJoinWithUNION( logicalOpBGPAdd );
        }
        else if ( subRootOp instanceof PhysicalOpBindJoinWithVALUES) {
            return new PhysicalOpBindJoinWithVALUES( logicalOpBGPAdd );
        }
        else
            throw new IllegalArgumentException("Unexpected type of physical operator (type: " + subRootOp.getClass().getName() + ").");
    }

}
