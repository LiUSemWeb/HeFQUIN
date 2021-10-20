package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import java.util.List;

public class PhysicalPlanWithCostUtils {

    public static PhysicalPlanWithCost findPlanWithLowestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
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

    public static PhysicalPlanWithCost findPlanWithHighestCost( final List<PhysicalPlanWithCost> plansWithCost ) {
        PhysicalPlanWithCost worstPlan = plansWithCost.get(0);
        double max = worstPlan.getWeight();

        for ( int i = 1; i < plansWithCost.size(); i++) {
            final PhysicalPlanWithCost p = plansWithCost.get(i);
            if ( p.getWeight() >= max ) {
                max = p.getWeight();
                worstPlan = p;
            }
        }
        return worstPlan;
    }

    public static double calculateAvgCostOfPlans( final List<PhysicalPlanWithCost> plansWithCost ) {
        double sum = 0;

        for ( final PhysicalPlanWithCost p : plansWithCost ) {
            sum += p.getWeight();
        }

        return sum/plansWithCost.size();
    }

}
