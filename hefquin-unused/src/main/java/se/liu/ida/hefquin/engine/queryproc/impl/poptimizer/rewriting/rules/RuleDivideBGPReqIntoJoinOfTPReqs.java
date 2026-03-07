package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
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

public class RuleDivideBGPReqIntoJoinOfTPReqs extends AbstractRewritingRuleImpl{

    public RuleDivideBGPReqIntoJoinOfTPReqs( final double priority ) {
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
                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

                final Set<TriplePattern> tps = LogicalOpUtils.getTriplePatternsOfReq( (LogicalOpRequest<?, ?>) lop);

                final Iterator<TriplePattern> it = tps.iterator();
                if ( tps.size() == 1 ) {
                    final TriplePatternRequest req1 = new TriplePatternRequestImpl( it.next() );
                    return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, req1) );
                }
                else if ( tps.size() > 1 ) {
                    final TriplePatternRequest req1 = new TriplePatternRequestImpl( it.next() );
                    final PhysicalPlan subPlan1 =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, req1) );

                    final TriplePatternRequest req2 = new TriplePatternRequestImpl( it.next() );
                    final PhysicalPlan subPlan2 =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, req2) );

                    PhysicalPlan subPlan = PhysicalPlanFactory.createPlanWithJoin( subPlan1, subPlan2);

                    while( it.hasNext() ) {
                        final TriplePatternRequest newReq = new TriplePatternRequestImpl( it.next() );
                        final PhysicalPlan newSubPlan =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
                        subPlan = PhysicalPlanFactory.createPlanWithJoin( subPlan, newSubPlan );
                    }
                    return subPlan;
                }
                else {
                    throw new IllegalArgumentException( "the BGP is empty" );
                }
            }
        };
    }

}
