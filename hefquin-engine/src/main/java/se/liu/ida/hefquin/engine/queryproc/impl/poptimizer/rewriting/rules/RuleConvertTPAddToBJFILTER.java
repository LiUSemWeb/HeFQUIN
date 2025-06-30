package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

public class RuleConvertTPAddToBJFILTER extends AbstractRewritingRuleImpl{

    public RuleConvertTPAddToBJFILTER( final double priority ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo( final PhysicalPlan plan ) {
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyPhysicalOpUsedForTPAdd.isIndexNLJ(rootOp) ) {
            final LogicalOpTPAdd tpAdd = (LogicalOpTPAdd) ((PhysicalOperatorForLogicalOperator)rootOp).getLogicalOperator();
            final FederationMember fm = tpAdd.getFederationMember();
            return (fm instanceof SPARQLEndpoint);
        }
        else return ( IdentifyPhysicalOpUsedForTPAdd.isBindJoinUNION(rootOp) || IdentifyPhysicalOpUsedForTPAdd.isBindJoinVALUES(rootOp) );

    }

    @Override
    protected RuleApplication createRuleApplication( final PhysicalPlan[] pathToTargetPlan ) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final PhysicalOperatorForLogicalOperator rootOp = (PhysicalOperatorForLogicalOperator) plan.getRootOperator();
                final LogicalOpTPAdd lop = (LogicalOpTPAdd) rootOp.getLogicalOperator();
                return PhysicalPlanFactory.createPlanWithBindJoinFILTER( lop , plan.getSubPlan(0) );
            }
        };
    }
}
