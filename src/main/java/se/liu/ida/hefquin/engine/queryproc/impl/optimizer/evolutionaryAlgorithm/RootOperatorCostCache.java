package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.evolutionaryAlgorithm;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RootOperatorCostCache {
    protected final Map<PhysicalPlan, List<Integer>> map = new HashMap<>();

    public void add ( final PhysicalPlan pp, final List<Integer> metrics ) {
        map.put( pp, metrics );
    }

    public boolean contains ( final PhysicalPlan pp ) {
        return map.containsKey(pp);
    }

    public List<Integer> get ( final PhysicalPlan pp ) {
        return map.get(pp);
    }
}
