package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Iterator;
import java.util.Set;

public class RuleDivideBGPAddToMultiTPAdd extends AbstractRewritingRuleImpl{

    public RuleDivideBGPAddToMultiTPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOpUsedForBGPAdd.matchBGPAdd(rootOp);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
                final FederationMember fm = rootLop.getFederationMember();

                final Set<TriplePattern> tps = (Set<TriplePattern>) rootLop.getBGP().getTriplePatterns();
                final Iterator<TriplePattern> it = tps.iterator();
                if ( tps.size() > 0 ) {
                    final DataRetrievalRequest initialReq = new TriplePatternRequestImpl( it.next() );
                    PhysicalPlan subPlan =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, initialReq) );

                    while( it.hasNext() ) {
                        final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, it.next() );
                        final PhysicalOperator tpAddOp = determinePhysicalOpOfRoot( rootOp, logicalTPAdd );
                        subPlan = PhysicalPlanFactory.createPlan( tpAddOp, subPlan );
                    }
                    return subPlan;
                }
                return null;
            }
        };
    }

    protected PhysicalOperator determinePhysicalOpOfRoot( final PhysicalOperator subRootOp, final LogicalOpTPAdd logicalOpTPAdd) {

        if ( subRootOp instanceof PhysicalOpIndexNestedLoopsJoin) {
            return new PhysicalOpIndexNestedLoopsJoin( logicalOpTPAdd );
        }
        else if ( subRootOp instanceof PhysicalOpBindJoinWithFILTER) {
            return new PhysicalOpBindJoinWithFILTER( logicalOpTPAdd );
        }
        else if ( subRootOp instanceof PhysicalOpBindJoinWithUNION) {
            return new PhysicalOpBindJoinWithUNION( logicalOpTPAdd );
        }
        else if ( subRootOp instanceof PhysicalOpBindJoinWithVALUES) {
            return new PhysicalOpBindJoinWithVALUES( logicalOpTPAdd );
        }
        else
            throw new IllegalArgumentException("Unexpected type of physical operator (type: " + subRootOp.getClass().getName() + ").");
    }

}
