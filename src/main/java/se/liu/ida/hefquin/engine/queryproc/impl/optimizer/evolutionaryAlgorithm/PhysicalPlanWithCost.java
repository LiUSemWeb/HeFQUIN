package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.utils.RandomizedSelection;

public class PhysicalPlanWithCost implements RandomizedSelection.WeightedObject {

    protected final PhysicalPlan plan;
    protected final double cost;

    public PhysicalPlanWithCost( final PhysicalPlan plan, final double cost ) {
        this.plan = plan;
        this.cost = cost;
    }

    public PhysicalPlan getPlan(){
        return this.plan;
    }

    @Override
    public double getWeight() {
        return this.cost;
    }

}
