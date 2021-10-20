package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public class Generation {

    public final List<PhysicalPlanWithCost> plans;
    public final PhysicalPlanWithCost bestPlan;
    public final PhysicalPlanWithCost worstPlan;
    public final double avgCost;
    //public final double stDeviation;

    public Generation( final List<PhysicalPlanWithCost> plans ) {
        this.plans = plans;
        this.bestPlan = PhysicalPlanWithCostUtils.findPlanWithLowestCost(plans);
        this.worstPlan = PhysicalPlanWithCostUtils.findPlanWithHighestCost(plans);
        this.avgCost = PhysicalPlanWithCostUtils.calculateAvgCostOfPlans(plans);
        //this.stDeviation = PhysicalPlanWithCostUtils.calculateStDevCostOfPlans( plans );
    }

}
