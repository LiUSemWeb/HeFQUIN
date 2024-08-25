package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public class DataStructureForStoringPlansOfSubsets
{
    protected Map< Integer, Map<List<PhysicalPlan>, PhysicalPlan> > map = new HashMap<>();

    public void add( final List<PhysicalPlan> subsets, final PhysicalPlan plan ) {
        final int size = subsets.size();
        final Map<List<PhysicalPlan>, PhysicalPlan> mapValue = map.get(size);

        if ( mapValue == null) {
            final Map<List<PhysicalPlan>, PhysicalPlan> mapTemp = new HashMap<>();
            mapTemp.put(subsets, plan);
            map.put(size, mapTemp);
        }
        else
            mapValue.put(subsets, plan);
    }

    public PhysicalPlan get( final List<PhysicalPlan> subsets ) {
        final Map<List<PhysicalPlan>, PhysicalPlan> mapValue = map.get( subsets.size() );
        return ( mapValue == null) ? null : mapValue.get(subsets);
    }

}
