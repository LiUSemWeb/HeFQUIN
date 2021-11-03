package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.DynamicProgramming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DPBasedOptimizerHelper {

    // This method returns all subsets (with the given size) of the given superset.
    public static <T> List<List<T>> getSubSet( final List<T> superset, final int n ){
        List<List<T>> result = new ArrayList<>();
        if( n==0 ){
            result.add( new ArrayList<>() );
            return result;
        }

        List<List<T>> tempList = new ArrayList<>();
        tempList.add( new ArrayList<>() );

        for ( int i = 0; i < superset.size(); i++ ){

            int size = tempList.size();
            for ( int j = 0; j < size; j++ ){
                // Stop adding more elements to the subset if its size is more than n.
                if( tempList.get(j).size() >= n ){
                    continue;
                }

                // Create a copy of the current subsets, then update the copy by adding an another element.
                List<T> clone = new ArrayList<>( tempList.get(j) );
                clone.add( superset.get(i));
                tempList.add(clone);

                // Add the subset to the result if its size is n.
                if( clone.size() == n ){
                    result.add(clone);
                }
            }
        }
        return result;
    }

    // Split a superset into two subsets. This method returns all possible combinations of subsets.
    public static <T> List<List<List<T>>> splitIntoSubSets( final List<T> superset ){
        List<List<T>> left = new ArrayList<>();
        List<List<T>> right = new ArrayList<>();
        List<List<List<T>>> result=new ArrayList<>();

        left.add(new ArrayList<>());
        right.add(new ArrayList<>(superset));

        for ( int i = 0; i < superset.size(); i++ ){
            int leftSize = left.size();
            for ( int j = 0; j < leftSize; j++ ){
                List<T> leftClone = new ArrayList<>( left.get(j) );
                leftClone.add( superset.get(i) );
                left.add(leftClone);

                List<T> rightClone = new ArrayList<>( right.get(j) );
                rightClone.remove( superset.get(i) );
                right.add( rightClone );

                result.add( Arrays.asList(new List[]{leftClone, rightClone}) );
            }
        }
        return result;
    }

}
