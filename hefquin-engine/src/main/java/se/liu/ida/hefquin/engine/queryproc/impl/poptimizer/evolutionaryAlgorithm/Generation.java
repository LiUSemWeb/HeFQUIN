package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm;

import java.util.List;

import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCost;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.utils.PhysicalPlanWithCostUtils;

public class Generation {

    public final List<PhysicalPlanWithCost> plans;
    public final PhysicalPlanWithCost bestPlan;
    public final PhysicalPlanWithCost worstPlan;
    public final double avgCost;
    public final int nrOfPlansWithBestCost;
//    public final double stDeviation;

    public Generation( final List<PhysicalPlanWithCost> plans ) {
        this.plans = plans;
        this.bestPlan = PhysicalPlanWithCostUtils.findPlanWithLowestCost(plans);
        this.worstPlan = PhysicalPlanWithCostUtils.findPlanWithHighestCost(plans);
        this.avgCost = PhysicalPlanWithCostUtils.calculateAvgCostOfPlans(plans);
        this.nrOfPlansWithBestCost = PhysicalPlanWithCostUtils.countNrOfPlansWithBestCost(plans, bestPlan.getWeight());
//        this.stDeviation = PhysicalPlanWithCostUtils.calculateStDevCostOfPlans( plans );
    }

}
