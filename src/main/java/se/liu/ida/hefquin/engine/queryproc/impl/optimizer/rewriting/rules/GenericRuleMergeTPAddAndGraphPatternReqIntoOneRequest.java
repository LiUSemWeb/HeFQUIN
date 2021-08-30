package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class GenericRuleMergeTPAddAndGraphPatternReqIntoOneRequest extends AbstractRewritingRuleImpl{

    public GenericRuleMergeTPAddAndGraphPatternReqIntoOneRequest( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();

        if( IdentifyPhysicalOpUsedForTPAdd.matchTPAdd(rootOp) ) {
            final LogicalOpTPAdd rootLop = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
            final FederationMember fm = rootLop.getFederationMember();

            if ( fm instanceof SPARQLEndpoint) {
                final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
                return subqueryIsGraphPatternReqWithSameFm( subRootOp, fm );
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
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpTPAdd lop = (LogicalOpTPAdd) rootOp.getLogicalOperator();
                final Triple tp = lop.getTP().asJenaTriple();

                final PhysicalOperatorForLogicalOperator subRootOp = (PhysicalOperatorForLogicalOperator) plan.getSubPlan(0).getRootOperator();
                final LogicalOpRequest subRootLop = (LogicalOpRequest) subRootOp.getLogicalOperator();
                final SPARQLQuery graphPattern = ((SPARQLRequest) subRootLop.getRequest()).getQuery();
                final Element e = graphPattern.asJenaQuery().getQueryPattern();
                // TODO: (P AND tp), TBD
                ((ElementGroup) e).addTriplePattern(tp);

                final SPARQLRequestImpl newReq = new SPARQLRequestImpl( new SPARQLGraphPatternImpl( e ));
                final FederationMember fm = lop.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

    static boolean subqueryIsGraphPatternReqWithSameFm(final PhysicalOperator subRootOp, final FederationMember fm ) {
        if ( subRootOp instanceof PhysicalOpRequest) {
            final LogicalOpRequest subLop = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

            return ( subLop.getFederationMember() == fm ) && ( subLop.getRequest() instanceof SPARQLRequest);
        }
        return false;
    }

}
