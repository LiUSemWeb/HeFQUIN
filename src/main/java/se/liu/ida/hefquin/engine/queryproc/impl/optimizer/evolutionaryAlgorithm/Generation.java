package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public class Generation {

    public final List<PhysicalPlanWithCost> plans;
    public final PhysicalPlanWithCost bestPlan;
    public final double avgCost;

    public Generation( final List<PhysicalPlanWithCost> plans ) {
        this.plans = plans;
        this.bestPlan = findPlanWithSmallestCost(plans);
        this.avgCost = calculateAvgCostOfPlans(plans);
    }

    public static PhysicalPlanWithCost findPlanWithSmallestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
        PhysicalPlanWithCost bestPlan = plansWithCost.get(0);
        double min = bestPlan.getWeight();

        for ( int i = 1; i < plansWithCost.size(); i++) {
            final PhysicalPlanWithCost p = plansWithCost.get(i);
            if ( p.getWeight() <= min ) {
                min = p.getWeight();
                bestPlan = p;
            }
        }
        return bestPlan;
    }

    public static double calculateAvgCostOfPlans( final List<PhysicalPlanWithCost> plansWithCost ) {
        double sum = 0;

        for ( final PhysicalPlanWithCost p : plansWithCost ) {
            sum += p.getWeight();
        }

        return sum/plansWithCost.size();
    }

}
