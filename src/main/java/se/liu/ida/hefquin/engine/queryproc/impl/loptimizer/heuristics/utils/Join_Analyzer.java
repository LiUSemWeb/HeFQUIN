package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;

import java.util.*;

public class Join_Analyzer {

    /**
     * Count number of different types of joins for a given plan;
     * - star joins: subject-subject and object-object joins
     * - chain joins: object-subject and subject-object joins
     * - unusual joins: subject-predicate, predicate-object, predicate-predicate joins
     */
    public static double countNumOfStarJoins( final Query_Analyzer plan ){
        return countDuplicates( plan.getSubs() )
                + countDuplicates( plan.getObjs() );
    }

    public static double countNumOfChainJoins( final Query_Analyzer plan ) {
        return countDuplicates( plan.getSubs(), plan.getObjs() );
    }

    public static double countNumOfUnusualJoins(final Query_Analyzer plan ) {
        return countDuplicates( plan.getSubs(), plan.getPreds() )
                + countDuplicates( plan.getPreds(), plan.getObjs() )
                + countDuplicates( plan.getPreds() );
    }

    /**
     * Count number of different types of joins between two given sub-plans
     */
    public static double countNumOfStarJoins( final Query_Analyzer plan_l, final Query_Analyzer plan_r ){
        return countDuplicates( plan_l.getSubs(), plan_r.getSubs() )
                + countDuplicates( plan_l.getObjs(), plan_r.getObjs() );
    }

    public static double countNumOfChainJoins( final Query_Analyzer plan_l, final Query_Analyzer plan_r ) {
        return countDuplicates( plan_l.getSubs(), plan_r.getObjs() )
                + countDuplicates( plan_l.getObjs(), plan_r.getSubs() );
    }

    public static double countNumOfUnusualJoins( final Query_Analyzer plan_l, final Query_Analyzer plan_r ) {
        return countDuplicates( plan_l.getSubs(), plan_r.getPreds() )
                + countDuplicates( plan_r.getSubs(), plan_l.getPreds() )
                + countDuplicates( plan_l.getPreds(), plan_r.getObjs() )
                + countDuplicates( plan_r.getPreds(), plan_l.getObjs() )
                + countDuplicates( plan_l.getPreds(), plan_r.getPreds() );
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
     * Count the number of pairs that have the same variable between two lists of variables
     */
    public static int countDuplicates( final List<Node> vars_a, final List<Node> vars_b ) {
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
