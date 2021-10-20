package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public class Generation {

    public final List<PhysicalPlanWithCost> plans;
    public final PhysicalPlanWithCost bestPlan;
    public final double avgCost;

    public Generation( final List<PhysicalPlanWithCost> plans ) {
        this.plans = plans;
        this.bestPlan = PhysicalPlanWithCost.findPlanWithSmallestCost(plans);
        this.avgCost = PhysicalPlanWithCost.calculateAvgCostOfPlans(plans);
    }

}
