package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.rules;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.Query;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementSubQuery;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLQuery;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGraphPatternImpl;
import se.liu.ida.hefquin.engine.queryplan.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.rewriting.RuleApplication;

public class RuleMergeBGPAddAndGraphPatternReqIntoOneRequest extends AbstractRewritingRuleImpl{

    public RuleMergeBGPAddAndGraphPatternReqIntoOneRequest( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();

        if( IdentifyPhysicalOpUsedForBGPAdd.matchBGPAdd(rootOp) ) {
            final LogicalOpBGPAdd rootLop = (LogicalOpBGPAdd) ((PhysicalOperatorForLogicalOperator) rootOp).getLogicalOperator();
            final FederationMember fm = rootLop.getFederationMember();

            final PhysicalOperator subRootOp = plan.getSubPlan(0).getRootOperator();
            return subqueryIsGraphPatternReqWithSameFm( subRootOp, fm );
        }

        return false;
    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpBGPAdd lop = (LogicalOpBGPAdd) rootOp.getLogicalOperator();
                //final Triple tp = lop.getTP().asJenaTriple();
                final BGP bgpOfBGPAdd = lop.getBGP();

                final PhysicalOperatorForLogicalOperator subRootOp = (PhysicalOperatorForLogicalOperator) plan.getSubPlan(0).getRootOperator();
                final LogicalOpRequest subRootLop = (LogicalOpRequest) subRootOp.getLogicalOperator();
                final SPARQLQuery graphPattern = ((SPARQLRequest) subRootLop.getRequest()).getQuery();
                final Element elementPattern = graphPattern.asJenaQuery().getQueryPattern();
                // TODO: (P AND B), TBD
                final ElementTriplesBlock elementBGP = new ElementTriplesBlock( ((OpBGP)bgpOfBGPAdd.asJenaOp()).getPattern() );
                ((ElementGroup) elementPattern).addElement(elementBGP);

                final SPARQLRequestImpl newReq = new SPARQLRequestImpl( new SPARQLGraphPatternImpl( elementPattern ));
                final FederationMember fm = lop.getFederationMember();

                return PhysicalPlanFactory.createPlanWithRequest( new LogicalOpRequest<>(fm, newReq) );
            }
        };
    }

    protected boolean subqueryIsGraphPatternReqWithSameFm(final PhysicalOperator subRootOp, final FederationMember fm ) {
        if ( subRootOp instanceof PhysicalOpRequest) {
            final LogicalOpRequest subLop = (LogicalOpRequest) ((PhysicalOperatorForLogicalOperator) subRootOp).getLogicalOperator();

            return ( subLop.getFederationMember() == fm ) && ( subLop.getRequest() instanceof SPARQLRequest);
        }
        return false;
    }

}
