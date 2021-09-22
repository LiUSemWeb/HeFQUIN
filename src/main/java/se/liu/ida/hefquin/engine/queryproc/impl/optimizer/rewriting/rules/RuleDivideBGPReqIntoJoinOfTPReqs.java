package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.BGPRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Iterator;
import java.util.Set;

public class RuleDivideBGPReqIntoJoinOfTPReqs extends AbstractRewritingRuleImpl{

    public RuleDivideBGPReqIntoJoinOfTPReqs( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        return IdentifyPhysicalOpUsedForReq.isBGPRequest(rootOp);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperator rootOp = plan.getRootOperator();
                final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
                final FederationMember fm = ((LogicalOpRequest<?, ?>) lop).getFederationMember();

                final BGPRequest req = (BGPRequest) ((LogicalOpRequest<?, ?>) lop).getRequest();
                final Set<TriplePattern> tps = (Set<TriplePattern>) req.getQueryPattern().getTriplePatterns();
                final Iterator<TriplePattern> it = tps.iterator();
                if ( tps.size() == 1 ) {
                    final DataRetrievalRequest req1 = new TriplePatternRequestImpl( it.next() );
                    return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, req1) );
                }
                else if ( tps.size() > 1 ) {
                    final DataRetrievalRequest req1 = new TriplePatternRequestImpl( it.next() );
                    PhysicalPlan subPlan1 =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, req1) );

                    final DataRetrievalRequest req2 = new TriplePatternRequestImpl( it.next() );
                    PhysicalPlan subPlan2 =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, req2) );

                    PhysicalPlan subPlan = PhysicalPlanFactory.createPlanWithJoin( subPlan1, subPlan2);

                    while( it.hasNext() ) {
                        final DataRetrievalRequest newReq = new TriplePatternRequestImpl( it.next() );
                        PhysicalPlan newSubPlan =  PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
                        subPlan = PhysicalPlanFactory.createPlanWithJoin( subPlan, newSubPlan );
                    }
                    return subPlan;
                }
                else {
                    return plan;
                }
            }
        };
    }

}
