package se.liu.ida.hefquin.engine.queryproc.impl.optimizer.DynamicProgramming;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DPBasedOptimizerHelper {

    // This method returns all subsets (with the given size) of the given superset.
    public static <T> List<List<T>> getSubSet( final List<T> superset, final int n ){
        final List<List<T>> result = new ArrayList<>();
        if( n==0 ){
            result.add( new ArrayList<>() );
            return result;
        }

        final List<List<T>> tempList = new ArrayList<>();
        tempList.add( new ArrayList<>() );

        for ( T element : superset ) {
            final int size = tempList.size();
            for ( int j = 0; j < size; j++ ){
                // Stop adding more elements to the subset if its size is more than n.
                if( tempList.get(j).size() >= n ){
                    continue;
                }

                // Create a copy of the current subsets, then update the copy by adding an another element.
                final List<T> clone = new ArrayList<>( tempList.get(j) );
                clone.add( element );
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
        final List<List<T>> left = new ArrayList<>();
        final List<List<T>> right = new ArrayList<>();
        final List<List<List<T>>> result = new ArrayList<>();

        left.add( new ArrayList<>() );
        right.add( new ArrayList<>(superset) );

        for ( T element : superset ) {
            final int leftSize = left.size();
            for ( int j = 0; j < leftSize; j++ ){
                final List<T> leftClone = new ArrayList<>( left.get(j) );
                leftClone.add( element );
                left.add( leftClone );

                final List<T> rightClone = new ArrayList<>( right.get(j) );
                rightClone.remove( element );
                right.add( rightClone );

                if ( leftClone.size() != 0 && rightClone.size() != 0 )
                    result.add( Arrays.asList( leftClone, rightClone ) );

            }
        }
        return result;
    }

}
