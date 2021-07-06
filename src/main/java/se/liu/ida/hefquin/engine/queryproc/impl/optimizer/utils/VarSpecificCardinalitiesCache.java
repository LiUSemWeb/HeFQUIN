package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.utils;

import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashMap;
import java.util.Map;

public class VarSpecificCardinalitiesCache {
    protected final Map<PhysicalPlan, Map<Var, Integer>> map = new HashMap<>();

    public boolean add ( final PhysicalPlan lop, final Var v, final Integer card ) {
        if (lop == null || v == null){
            throw new IllegalArgumentException();
        }

        final Map<Var, Integer> mapIn = map.get(lop);
        if ( mapIn == null ){
            final Map<Var, Integer> mapV = new HashMap<>();
            mapV.put(v, card);
            map.put(lop, mapV);
        }
        else mapIn.put(v, card);

        return true;
    }

    public boolean contains ( final PhysicalPlan lop, final Var v ) {
        if ( map.containsKey(lop) ){
            Map<Var, Integer> mapV = map.get(lop);
            if( mapV.containsKey(v) ){ return true; }
            return false;
        }
        return false;
    }

    public Integer get ( final PhysicalPlan lop, final Var v ) {
        if ( map.containsKey(lop) ){
            Map<Var, Integer> mapV = map.get(lop);
            if(mapV.containsKey(v)){
                return mapV.get(v);
            }
            return null;
        }
        return null;
    }

}
