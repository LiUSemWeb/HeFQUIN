package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalOpUtils;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;

import java.util.Iterator;
import java.util.Set;

public class RuleDivideBGPReqIntoMultiTPAdds extends AbstractRewritingRuleImpl{

    public RuleDivideBGPReqIntoMultiTPAdds( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyTypeOfRequestUsedForReq.isBGPRequest(rootOp);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();

                final Set<TriplePattern> tps = LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) lop);

                final Iterator<TriplePattern> it = tps.iterator();
                if ( tps.size() == 0 ) {
                    throw new IllegalArgumentException( "the BGP is empty" );
                }

                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();
                final TriplePatternRequest initialReq = new TriplePatternRequestImpl( it.next() );
                PhysicalPlan subPlan =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, initialReq) );
                while( it.hasNext() ) {
                    final LogicalOpTPAdd logicalTPAdd = new LogicalOpTPAdd( fm, it.next() );
                    subPlan = PhysicalPlanFactory.createPlan( logicalTPAdd, subPlan );
                }
                return subPlan;
            }
        };
    }

}
