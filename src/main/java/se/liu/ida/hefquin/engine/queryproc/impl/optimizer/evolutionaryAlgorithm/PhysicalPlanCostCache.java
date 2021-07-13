package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashMap;
import java.util.Map;

public class PhysicalPlanCostCache {
    protected final Map<PhysicalPlan, Metrics> map = new HashMap<>();

    public void add ( final PhysicalPlan pp, final Metrics metrics ) {
        map.put( pp, metrics);
    }

    public boolean contains ( final PhysicalPlan pp ) {
        return map.containsKey(pp);
    }

    public Metrics get ( final PhysicalPlan pp ) {
        return map.get(pp);
    }
}