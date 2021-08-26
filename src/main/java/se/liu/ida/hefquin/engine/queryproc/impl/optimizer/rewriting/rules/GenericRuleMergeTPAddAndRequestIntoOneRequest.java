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
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

import java.util.Set;

public abstract class GenericRuleMergeTPAddAndRequestIntoOneRequest extends AbstractRewritingRuleImpl{

    public GenericRuleMergeTPAddAndRequestIntoOneRequest( final double priority ) {
        super(priority);
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final LogicalOpTPAdd lop = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator) plan.getRootOperator()).getLogicalOperator();
                final TriplePattern tp = lop.getTP();

                final LogicalOpRequest subRootOp = (LogicalOpRequest) plan.getSubPlan(0).getRootOperator();
                final BGP bgp = ((BGPRequest) subRootOp.getRequest()).getQueryPattern();
                final Set<TriplePattern> tps = (Set<TriplePattern>) bgp.getTriplePatterns();
                tps.add(tp);

                final DataRetrievalRequest req = new BGPRequestImpl( new BGPImpl(tps) );
                final FederationMember fm = lop.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm,req) );
            }
        };
    }

    static boolean subqueryIsARequestWithSameFm( final PhysicalOperator subRootOp, final FederationMember fm) {
        if ( subRootOp instanceof PhysicalOpRequest) {
            final LogicalOpRequest subLop = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

            return ( subLop.getFederationMember() == fm ) && ( subLop.getRequest() instanceof BGPRequest);
        }
        return false;
    }

}
