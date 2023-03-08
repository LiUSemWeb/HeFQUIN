package se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple;

import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.QueryOptimizationContext;
import se.liu.ida.hefquin.engine.utils.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DPBasedLinearJoinPlanOptimizer extends DPBasedJoinPlanOptimizer {

    public DPBasedLinearJoinPlanOptimizer( final QueryOptimizationContext ctxt ) {
        super(ctxt);
    }

    @Override
    public <T> List<Pair<List<T>, List<T>>> splitIntoSubSets( final List<T> superset ) {
        if ( superset.size() < 2 ){
            throw new IllegalArgumentException("Cannot divide a set of length less than two into two non-empty subsets. (length: " + superset.size() + ").");
        }

        final List< Pair<List<T>, List<T>> > result = new ArrayList<>();
        for ( T element : superset ) {
            final List<T> right = Arrays.asList(element);
            final List<T> left = new ArrayList(superset);
            left.remove(element);

            result.add( new Pair<>(left, right));
        }
        return result;
    }

}
