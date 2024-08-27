package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.ArrayList;
import java.util.List;

public class DPBasedBushyJoinPlanOptimizer extends DPBasedJoinPlanOptimizer {

    public DPBasedBushyJoinPlanOptimizer(final CostModel costModel ) {
        super(costModel);
    }

    @Override
    public <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset ) {
        if ( superset.size() < 2 ){
            throw new IllegalArgumentException("Cannot divide a set of length less than two into two non-empty subsets. (length: " + superset.size() + ").");
        }

        final List<List<T>> left = new ArrayList<>();
        final List<List<T>> right = new ArrayList<>();
        final List< Pair<List<T>, List<T>> > result = new ArrayList<>();

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
                    result.add( new Pair<>( leftClone, rightClone ) );

            }
        }
        return result;
    }

}
