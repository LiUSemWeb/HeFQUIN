package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashMap;
import java.util.Map;

public class VarSpecificCardinalitiesCache {
    protected final Map<PhysicalPlan, Map<Var, Integer>> map = new HashMap<>();

    public void add ( final PhysicalPlan pp, final Var v, final Integer card ) {
        if ( pp == null || v == null ){
            throw new IllegalArgumentException();
        }

        final Map<Var, Integer> mapIn = map.get(pp);
        if ( mapIn == null ){
            final Map<Var, Integer> mapV = new HashMap<>();
            mapV.put(v, card);
            map.put(pp, mapV);
        }
        else mapIn.put(v, card);
    }

    public boolean contains ( final PhysicalPlan pp, final Var v ) {
        Map<Var, Integer> mapV = map.get(pp);
        return mapV.containsKey(v);
    }

    public Integer get ( final PhysicalPlan pp, final Var v ) {
        Map<Var, Integer> mapV = map.get(pp);
        return mapV.get(v);
    }
}
