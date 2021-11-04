package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.DynamicProgramming;

import org.junit.Test;
import se.liu.ida.hefquin.engine.EngineTestBase;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class DPBasedOptimizerHelperTest extends EngineTestBase {
    final List<Integer> list= List.of( new Integer[]{1, 2, 3, 4, 5} );

    @Test
    public void getSubsets_1()
    {
        final List<List<Integer>> subsets = DPBasedOptimizerHelper.getSubSet( list, 3 );

        assertEquals( 10, subsets.size() );
        subsets.forEach( System.out::println );
    }

    @Test
    public void getSubsets_2()
    {
        final List<List<Integer>> subsets = DPBasedOptimizerHelper.getSubSet( list, 0 );

        assertEquals( 1, subsets.size() );
        subsets.forEach( System.out::println );
    }

    @Test
    public void getSubsets_3()
    {
        final List<List<Integer>> subsets = DPBasedOptimizerHelper.getSubSet( list, 7 );

        assertEquals( 0, subsets.size() );
        subsets.forEach( System.out::println );
    }

    @Test
    public void splitIntoTwoSubSets()
    {
        final List<List<List<Integer>>> subsets = DPBasedOptimizerHelper.splitIntoSubSets(list);

        assertEquals( 30, subsets.size() );

        subsets.forEach(l->{
            System.out.println( l.get(0) );
            System.out.println( l.get(1) );
            System.out.println("--------");
        });
    }

}
