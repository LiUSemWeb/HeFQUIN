package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.DynamicProgramming;

import org.junit.Test;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.simple.DPBasedJoinPlanOptimizer;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DPBasedOptimizerHelperTest extends EngineTestBase {
    final List<Integer> list= List.of( new Integer[]{1, 2, 3, 4, 5} );

    @Test
    public void getSubsets_1()
    {
        final List<List<Integer>> subsets = DPBasedJoinPlanOptimizer.getSubSet( list, 3 );

        assertEquals( 10, subsets.size() );
        subsets.forEach( System.out::println );
    }

    @Test
    public void getSubsets_2()
    {
        final List<List<Integer>> subsets = DPBasedJoinPlanOptimizer.getSubSet( list, 0 );

        assertEquals( 1, subsets.size() );
        subsets.forEach( System.out::println );
    }

    @Test
    public void getSubsets_3()
    {
        final List<List<Integer>> subsets = DPBasedJoinPlanOptimizer.getSubSet( list, 7 );

        assertEquals( 0, subsets.size() );
        subsets.forEach( System.out::println );
    }

    @Test
    public void splitIntoTwoSubSets()
    {
        final List<Pair<List<Integer>, List<Integer>>> subsets = DPBasedJoinPlanOptimizer.splitIntoSubSets(list);

        assertEquals( 30, subsets.size() );

        for ( Pair l: subsets ){
            System.out.println( l.object1 );
            System.out.println( l.object2 );
            System.out.println("--------");
        }
    }

}
