package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeJoinOfTwoPatternReqIntoOneReq extends AbstractRewritingRuleImpl{

    public RuleMergeJoinOfTwoPatternReqIntoOneReq( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
        if (rootOp.getLogicalOperator() instanceof LogicalOpJoin) {

            final PhysicalOperator subPlanOp1 = plan.getSubPlan(0).getRootOperator();
            final PhysicalOperator subPlanOp2 = plan.getSubPlan(1).getRootOperator();

            if ( isGraphPatternRequest( subPlanOp1 ) ) {
                final FederationMember fm = ((LogicalOpRequest) ((PhysicalOperatorForLogicalOperator)subPlanOp1).getLogicalOperator()).getFederationMember();

                return isGraphPatternReqWithSameFm( subPlanOp2, fm );
            }
            return false;
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                PhysicalPlan subPlan1 = plan.getSubPlan(0);
                PhysicalPlan subPlan2 = plan.getSubPlan(1);

                final LogicalOperator subPlanLop1 = ((PhysicalOperatorForLogicalOperator) subPlan1.getRootOperator()).getLogicalOperator();
                final LogicalOperator subPlanLop2 = ((PhysicalOperatorForLogicalOperator) subPlan2.getRootOperator()).getLogicalOperator();

                final SPARQLGraphPattern newGraphPattern = createNewGraphPattern( subPlanLop1, subPlanLop2 );
                final SPARQLRequestImpl newReq = new SPARQLRequestImpl( newGraphPattern );

                final FederationMember fm = ((LogicalOpRequest<?, ?>) subPlanLop1).getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

    protected boolean isGraphPatternRequest( final PhysicalOperator op ) {
        final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
        if( lop instanceof LogicalOpRequest){
            return ((LogicalOpRequest<?, ?>) lop).getRequest() instanceof SPARQLRequest;
        }
        return false;
    }

    protected boolean isGraphPatternReqWithSameFm(final PhysicalOperator op, final FederationMember fm ) {
        if ( isGraphPatternRequest(op) ){
            final LogicalOperator lop = ((PhysicalOperatorForLogicalOperator) op).getLogicalOperator();
            return ((LogicalOpRequest)lop).getFederationMember() == fm;
        }
        return false;
    }

    protected SPARQLGraphPattern createNewGraphPattern(final LogicalOperator lopReq1, final LogicalOperator lopReq2 ) {
        // check the type of Logical operator
        final SPARQLQuery graphPattern1 = ((SPARQLRequest) ((LogicalOpRequest)lopReq1).getRequest()).getQuery();
        final Element element1 = graphPattern1.asJenaQuery().getQueryPattern();

        final SPARQLQuery graphPattern2 = ((SPARQLRequest) ((LogicalOpRequest)lopReq2).getRequest()).getQuery();
        final Element element2 = graphPattern2.asJenaQuery().getQueryPattern();

        ((ElementGroup) element1).addElement(element2);

        return new SPARQLGraphPatternImpl(element1);
    }

}
