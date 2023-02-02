package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Join_Analyzer {

    // Consider the subject-subject and object-object joins as star joins
    public static double getNumOfStarJoins( final List<Node> vars_s, final List<Node> vars_o ){
        final int jsubs = countDuplicates(vars_s);
        final int jobjs = countDuplicates(vars_o);
        return ( jsubs + jobjs );
    }

    // Consider the object-subject and subject-object joins as chain joins
    public static double getNumOfChainJoins( final List<Node> vars_s, final List<Node> vars_o ) {
        return countDuplicates(vars_s,vars_o);
    }

    // Consider subject-predicate, predicate-object, predicate-predicate joins as unusual joins
    public static double getNumOfUnusualJoins( final List<Node> vars_s, final List<Node> vars_p, final List<Node> vars_o ) {
        return countDuplicates(vars_s,vars_p) + countDuplicates(vars_p,vars_o)+ countDuplicates(vars_p);
    }

    /**
     * Sum the number of duplicates for each variable
     * e.g., if the input contains three ?s variable, the number of duplicates is 2
     */
    private static int countDuplicates( final List<Node> vars ) {
        final Map<Node,Integer> map= new HashMap<>();

        int count = 0;
        for ( final Node s: vars ) {
            if ( map.containsKey(s) ) {
                count = map.get(s) + 1;
            }
            map.put( s, count );
        }

        int sum = 0;
        for ( final Node s: map.keySet() ) {
            if( map.get(s) > 0 ) {
                sum += map.get(s);
            }
        }

        return sum;
    }

    /**
     * Count the number of matched pairs between two lists of variables
     */
    private static int countDuplicates( final List<Node> vars_a, final List<Node> vars_b ) {
        final Map<Node,Integer> map= new HashMap<>();

        for ( final Node a: vars_a ) {
            final int count_a = map.containsKey(a)? (map.get(a) + 1) : 1;
            map.put( a, count_a );
        }

        int sum = 0;
        for ( final Node b: vars_b ) {
            if ( map.containsKey(b) && map.get(b) > 0 ) {
                sum += 1;
                map.put( b, map.get(b)-1 );
            }
        }

        return sum;
    }

}
