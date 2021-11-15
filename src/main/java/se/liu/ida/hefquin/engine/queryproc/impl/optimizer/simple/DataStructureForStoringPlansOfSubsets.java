package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataStructureForStoringPlansOfSubsets {
    protected Map< Integer, Map<List<PhysicalPlan>, PhysicalPlan> > map=new HashMap<>();

    public void add( final List<PhysicalPlan> subsets, final PhysicalPlan plan ) {
        final int size = subsets.size();
        final Map<List<PhysicalPlan>, PhysicalPlan> mapValue = map.get(size);

        if ( mapValue == null)
            map.put( size, new HashMap<>() );
        else
            mapValue.put( subsets, plan );
    }

    public PhysicalPlan get( final List<PhysicalPlan> subsets ) {
        final Map<List<PhysicalPlan>, PhysicalPlan> mapValue = map.get(subsets.size());

        if ( mapValue == null)
            return null;
        else
            return mapValue.get( subsets );

    }

}
