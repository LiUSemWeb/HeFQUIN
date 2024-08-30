package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.rules;

import se.liu.ida.hefquin.engine.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.rewriting.RuleApplication;

public class RuleConvertUnionOfTwoJoinsToJoinOfUnion extends AbstractRewritingRuleImpl {

    public RuleConvertUnionOfTwoJoinsToJoinOfUnion(  final double priority  ) {
        super(priority);
    }

    @Override
    protected boolean canBeAppliedTo(PhysicalPlan plan) {
        // union of two joins, and the two joins share a same subquery
        final PhysicalOperator rootOp = plan.getRootOperator();
        if ( IdentifyLogicalOp.isUnion(rootOp) ) {
            final PhysicalPlan subPlan1 = plan.getSubPlan(0);
            final PhysicalPlan subPlan2 = plan.getSubPlan(1);
            if ( IdentifyLogicalOp.isJoin( subPlan1.getRootOperator() )
                    && IdentifyLogicalOp.isJoin( subPlan2.getRootOperator() ) ) {
                return subPlan1.getSubPlan(0) == subPlan2.getSubPlan(0)
                        || subPlan1.getSubPlan(0) == subPlan2.getSubPlan(1)
                        || subPlan1.getSubPlan(1) == subPlan2.getSubPlan(0)
                        || subPlan1.getSubPlan(1) == subPlan2.getSubPlan(1);
            }
        }
        return false;
    }

    @Override
    protected RuleApplication createRuleApplication(PhysicalPlan[] pathToTargetPlan) {
        return new AbstractRuleApplicationImpl(pathToTargetPlan, this) {
            @Override
            protected PhysicalPlan rewritePlan( final PhysicalPlan plan ) {
                final BinaryPhysicalOp rootOp = (BinaryPhysicalOp) plan.getRootOperator();
                final PhysicalPlan subPlan1 = plan.getSubPlan(0);
                final PhysicalPlan subPlan2 = plan.getSubPlan(1);

                if ( subPlan1.getSubPlan(0) == subPlan2.getSubPlan(0) ) {
//                    Construct a new subPlan with Union as root operator
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( rootOp, subPlan1.getSubPlan(1) , subPlan2.getSubPlan(1) );

                    return PhysicalPlanFactory.createPlan( subPlan1.getRootOperator(), subPlan1.getSubPlan(0), newSubPlan );
                }
                else if ( subPlan1.getSubPlan(0) == subPlan2.getSubPlan(1) ) {
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( rootOp, subPlan1.getSubPlan(1) , subPlan2.getSubPlan(0) );

                    return PhysicalPlanFactory.createPlan( subPlan1.getRootOperator(), subPlan1.getSubPlan(0), newSubPlan );
                }
                else if ( subPlan1.getSubPlan(1) == subPlan2.getSubPlan(0) ) {
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( rootOp, subPlan1.getSubPlan(0) , subPlan2.getSubPlan(1) );

                    return PhysicalPlanFactory.createPlan( subPlan1.getRootOperator(), subPlan1.getSubPlan(1), newSubPlan );
                }
                else if ( subPlan1.getSubPlan(1) == subPlan2.getSubPlan(1) ) {
                    final PhysicalPlan newSubPlan = PhysicalPlanFactory.createPlan( rootOp, subPlan1.getSubPlan(0) , subPlan2.getSubPlan(0) );

                    return PhysicalPlanFactory.createPlan( subPlan1.getRootOperator(), subPlan1.getSubPlan(1), newSubPlan );
                }
                else  {
                    return plan;
                }
            }
        };
    }
}
