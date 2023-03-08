package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import org.junit.Test;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;

import static org.junit.Assert.*;

public class DPBasedOptimizerHelperTest extends EngineTestBase {
    final List<Integer> list= List.of( new Integer[]{1, 2, 3, 4, 5} );

    final List<Integer> list_three= List.of( new Integer[]{1, 2, 3} );
    final QueryOptimizationContext ctxt = new QueryOptimizationContext() {
        @Override public FederationCatalog getFederationCatalog() { return null; }
        @Override public FederationAccessManager getFederationAccessMgr() { return null; }
        @Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
        @Override public boolean isExperimentRun() { return false; }
        @Override public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { return null; }
        @Override public CostModel getCostModel() { return null; }
    };

    @Test
    public void getSubsets_1()
    {
        final List<List<Integer>> subsets = DPBasedJoinPlanOptimizer.getSubSet( list, 3 );

        assertEquals( 10, subsets.size() );

        assertEquals( Arrays.toString( new Integer[] { 1, 2, 3 }), Arrays.toString( subsets.get(0).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 1, 2, 4 }), Arrays.toString( subsets.get(1).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 1, 3, 4 }), Arrays.toString( subsets.get(2).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 2, 3, 4 }), Arrays.toString( subsets.get(3).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 1, 2, 5 }), Arrays.toString( subsets.get(4).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 1, 3, 5 }), Arrays.toString( subsets.get(5).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 2, 3, 5 }), Arrays.toString( subsets.get(6).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 1, 4, 5 }), Arrays.toString( subsets.get(7).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 2, 4, 5 }), Arrays.toString( subsets.get(8).toArray() ));
        assertEquals( Arrays.toString( new Integer[] { 3, 4, 5 }), Arrays.toString( subsets.get(9).toArray() ));
    }

    @Test
    public void getSubsets_2()
    {
        try {
            final List<List<Integer>> subsets = DPBasedJoinPlanOptimizer.getSubSet( list, 0 );
        } catch ( IllegalArgumentException ex ) {
            assertEquals( "Does not support to get subsets with less than one element or containing more than the total number of elements in the superset (length of subset: 0).", ex.getMessage());
        }

    }

    @Test
    public void getSubsets_3()
    {
        try {
            final List<List<Integer>> subsets = DPBasedJoinPlanOptimizer.getSubSet(list, 7);
        } catch ( IllegalArgumentException ex ) {
            assertEquals( "Does not support to get subsets with less than one element or containing more than the total number of elements in the superset (length of subset: 7).", ex.getMessage());
        }

    }

    @Test
    public void splitIntoTwoSubSets_LinearTest()
    {
        final List<Pair<List<Integer>, List<Integer>>> subsets = new DPBasedLinearJoinPlanOptimizer(ctxt).splitIntoSubSets(list_three);

        assertEquals( 3, subsets.size() );

        assertEquals( Arrays.toString( new Integer[] { 2, 3 }), Arrays.toString( subsets.get(0).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 1 }), Arrays.toString( subsets.get(0).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 1, 3 }), Arrays.toString( subsets.get(1).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 2 }), Arrays.toString( subsets.get(1).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 1, 2 }), Arrays.toString( subsets.get(2).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 3 }), Arrays.toString( subsets.get(2).object2.toArray(new Integer[0])));
    }

    @Test
    public void splitIntoTwoSubSets_BushyTest()
    {
        final List<Pair<List<Integer>, List<Integer>>> subsets = new DPBasedBushyJoinPlanOptimizer(ctxt).splitIntoSubSets(list_three);

        assertEquals( 6, subsets.size() );

        assertEquals( Arrays.toString( new Integer[] { 1 }), Arrays.toString( subsets.get(0).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 2, 3 }), Arrays.toString( subsets.get(0).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 2 }), Arrays.toString( subsets.get(1).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 1, 3 }), Arrays.toString( subsets.get(1).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 1, 2 }), Arrays.toString( subsets.get(2).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 3 }), Arrays.toString( subsets.get(2).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 3 }), Arrays.toString( subsets.get(3).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 1, 2 }), Arrays.toString( subsets.get(3).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 1, 3 }), Arrays.toString( subsets.get(4).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 2 }), Arrays.toString( subsets.get(4).object2.toArray(new Integer[0])));

        assertEquals( Arrays.toString( new Integer[] { 2, 3 }), Arrays.toString( subsets.get(5).object1.toArray(new Integer[0])));
        assertEquals( Arrays.toString( new Integer[] { 1 }), Arrays.toString( subsets.get(5).object2.toArray(new Integer[0])));
    }

}
