package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;

import java.util.*;

public class JoinAnalyzer {

    /**
     * Count number of different types of joins for a given plan;
     * - s-s joins: subject-subject
     * - o-o joins: object-object joins
     * - chain joins: object-subject and subject-object joins
     * - unusual joins: subject-predicate, predicate-object, predicate-predicate joins
     */
    public static int countNumOfJoinsWithSameSub( final QueryAnalyzer plan ){
        return countDuplicates( plan.getSubs() );
    }

    public static int countNumOfJoinsWithSameObj( final QueryAnalyzer plan ){
        return countDuplicates( plan.getObjs() );
    }

    public static int countNumOfChainJoins( final QueryAnalyzer plan ) {
        return countNumOfJoinPairs( plan.getSubs(), plan.getObjs() );
    }

    public static int countNumOfUnusualJoins( final QueryAnalyzer plan ) {
        return countNumOfJoinPairs( plan.getSubs(), plan.getPreds() )
                + countNumOfJoinPairs( plan.getPreds(), plan.getObjs() )
                + countDuplicates( plan.getPreds() );
    }

    /**
     * Count number of different types of joins between two given sub-plans
     */
    public static int countNumOfJoinsWithSameSub( final QueryAnalyzer plan_l, final QueryAnalyzer plan_r ){
        return countNumOfJoinPairs( plan_l.getSubs(), plan_r.getSubs() );
    }

    public static int countNumOfJoinsWithSameObj( final QueryAnalyzer plan_l, final QueryAnalyzer plan_r ){
        return countNumOfJoinPairs( plan_l.getObjs(), plan_r.getObjs() );
    }

    public static int countNumOfChainJoins( final QueryAnalyzer plan_l, final QueryAnalyzer plan_r ) {
        return countNumOfJoinPairs( plan_l.getSubs(), plan_r.getObjs() )
                + countNumOfJoinPairs( plan_l.getObjs(), plan_r.getSubs() );
    }

    public static int countNumOfUnusualJoins( final QueryAnalyzer plan_l, final QueryAnalyzer plan_r ) {
        return countNumOfJoinPairs( plan_l.getSubs(), plan_r.getPreds() )
                + countNumOfJoinPairs( plan_r.getSubs(), plan_l.getPreds() )
                + countNumOfJoinPairs( plan_l.getPreds(), plan_r.getObjs() )
                + countNumOfJoinPairs( plan_r.getPreds(), plan_l.getObjs() )
                + countNumOfJoinPairs( plan_l.getPreds(), plan_r.getPreds() );
    }

    /**
     * Sum the number of duplicates for each variable
     * e.g., if the input contains three ?s variable, the number of duplicates is 2
     */
    public static int countDuplicates( final List<Node> vars ) {
        final Set<Node> uniqueVars = new HashSet<>();

        int sum = 0;
        for ( final Node s: vars ) {
            if ( !uniqueVars.contains(s) ) {
                uniqueVars.add(s);
            }
            else
                sum += 1;
        }

        return sum;
    }

    /**
     * For each occurrence of a variable in the second list, check whether there exists a matching partner (the same variable) in the first list.
     * If exists, increase the "numOfJoinPairs" by 1, and remove the matched variable (instance) from the first list.
     * Otherwise, move to the next variable.
     *
     * Repeat this process until all occurrences of variables in the second list have been checked.
     *
     * Note: If a variable occurs multiple times, each occurrence is counted in join pairs only once.
     */
    public static int countNumOfJoinPairs( final List<Node> vars_a, final List<Node> vars_b ) {
        final List<Node> intersection = new ArrayList<>(vars_a);
        intersection.retainAll(vars_b);

        if( intersection.isEmpty() ) {
            // The given two lists have no intersection
            return 0;
        }

        final Map<Node,Integer> map= new HashMap<>();

        for ( final Node a: vars_a ) {
            final int count_a = map.containsKey(a)? (map.get(a) + 1) : 1;
            map.put( a, count_a );
        }

        int numOfJoinPairs = 0;
        for ( final Node b: vars_b ) {
            final Integer bCounter = map.get(b);
            if ( bCounter != null && bCounter > 0 ) {
                numOfJoinPairs ++;
                map.put( b, bCounter-1 );
            }
        }

        return numOfJoinPairs;
    }

}
