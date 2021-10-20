package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.utils.RandomizedSelection;

import java.util.List;

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
