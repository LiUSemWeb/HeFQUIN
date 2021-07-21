package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.*;

public class CardinalitiesCache
{
    protected final Map<PhysicalPlan, Integer> map = new HashMap<>();

    public synchronized void add( final PhysicalPlan pp, final Integer card ) {
        map.put( pp, card );
    }

    public synchronized boolean contains( final PhysicalPlan pp ) {
        return map.containsKey(pp);
    }

    public synchronized Integer get( final PhysicalPlan pp ) {
        return map.get(pp);
    }

}
