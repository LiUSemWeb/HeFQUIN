package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm.rewritingRule;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithBinaryRootImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.ConstructRequestBasedOnUnaryOperator;

public abstract class ConvertTPAddToHashJoin implements Rule {
    protected final ConstructRequestBasedOnUnaryOperator helper = new ConstructRequestBasedOnUnaryOperator();

    @Override
    public PhysicalPlan applyTo( final PhysicalPlan pp ) {
        final PhysicalOperatorForLogicalOperator pop = (PhysicalOperatorForLogicalOperator) pp.getRootOperator();
        final LogicalOpTPAdd lop = (LogicalOpTPAdd) pop.getLogicalOperator();

        final PhysicalOpHashJoin popJoin = new PhysicalOpHashJoin( new LogicalOpJoin() );
        final PhysicalPlan subqueryRequest = helper.formRequestBasedOnTPofTPAdd( lop );
        final PhysicalPlan ppNew = new PhysicalPlanWithBinaryRootImpl( popJoin, subqueryRequest, pp.getSubPlan(0));

        return ppNew;
    }

    @Override
    public Double getPriority() {
        return 0.15;
    }

}
