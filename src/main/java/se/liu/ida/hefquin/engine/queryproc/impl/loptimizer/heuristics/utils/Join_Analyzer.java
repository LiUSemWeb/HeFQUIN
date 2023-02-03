package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;

import java.util.*;

public class Join_Analyzer {

    // Count number of star joins ( i.e., subject-subject and object-object joins )
    public static double getNumOfStarJoins( final List<Node> vars_s, final List<Node> vars_o ){
        final int jsubs = countDuplicates(vars_s);
        final int jobjs = countDuplicates(vars_o);
        return ( jsubs + jobjs );
    }

    // Count number of chain joins (i.e., object-subject and subject-object joins )
    public static double getNumOfChainJoins( final List<Node> vars_s, final List<Node> vars_o ) {
        return countDuplicates(vars_s,vars_o);
    }

    // Count number of unusual joins (i.e., subject-predicate, predicate-object, predicate-predicate joins )
    public static double getNumOfUnusualJoins( final List<Node> vars_s, final List<Node> vars_p, final List<Node> vars_o ) {
        return countDuplicates(vars_s,vars_p) + countDuplicates(vars_p,vars_o)+ countDuplicates(vars_p);
    }

    /**
     * Sum the number of duplicates for each variable
     * e.g., if the input contains three ?s variable, the number of duplicates is 2
     */
    private static int countDuplicates( final List<Node> vars ) {
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
     * Count the number of pairs that have the same variable between two lists of variables
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
