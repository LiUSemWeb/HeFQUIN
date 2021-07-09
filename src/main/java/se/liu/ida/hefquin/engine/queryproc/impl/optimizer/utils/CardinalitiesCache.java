package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.*;

public class CardinalitiesCache {
    protected final Map<PhysicalPlan, Integer> map = new HashMap<>();

    public void add ( final PhysicalPlan lp, final Integer card ) {
        map.put( lp, card );
    }

    public boolean contains ( final PhysicalPlan lp ) {
        return map.containsKey(lp);
    }

    public Integer get ( PhysicalPlan lp) {
        return map.get(lp);
    }

}
