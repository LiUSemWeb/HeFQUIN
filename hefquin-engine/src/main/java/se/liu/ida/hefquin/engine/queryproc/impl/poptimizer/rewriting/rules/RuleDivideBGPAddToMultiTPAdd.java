package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.federation.FederationMember;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RuleDivideBGPAddToMultiTPAdd extends AbstractRewritingRuleImpl{

    public RuleDivideBGPAddToMultiTPAdd( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyLogicalOp.isBGPAdd(rootOp);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
                final FederationMember fm = rootLop.getFederationMember();

                final Set<TriplePattern> tps = new HashSet<>( rootLop.getBGP().getTriplePatterns() );

                if ( tps.size() == 0 ) {
                    throw new IllegalArgumentException( "the BGP is empty" );
                }

                final Iterator<TriplePattern> it = tps.iterator();
                PhysicalPlan subPlan = plan.getSubPlan(0);

                while( it.hasNext() ) {
                    final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, it.next() );
                    subPlan = PhysicalPlanFactory.createPlan( logicalTPAdd, subPlan );
                }
                return subPlan;
            }
        };
    }

}
