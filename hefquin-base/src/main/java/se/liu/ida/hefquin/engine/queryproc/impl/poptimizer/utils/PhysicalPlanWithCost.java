package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.utils.RandomizedSelection;

import java.util.Objects;

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

    @Override
    public int hashCode(){
        return plan.hashCode() ^ Objects.hash(cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PhysicalPlanWithCost that = (PhysicalPlanWithCost) o;
        return plan.equals(that.plan) && cost == that.cost;
    }

}
