package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Join_Analyzer {

    public static double getStarJoins( final List<Node> vars_s, final List<Node> vars_o, final double J_Ts){
        final int jsubs = countDuplicates(vars_s);
        final int jobjs = countDuplicates(vars_o);
        return ( jsubs + jobjs ) * J_Ts;
    }

    public static double getChainJoins( final List<Node> vars_s, final List<Node> vars_o, final double J_Tc) {
        final int chainjoins = countDuplicates(vars_s,vars_o);
        return chainjoins * J_Tc;
    }

    public static double getUnusualJoins(final List<Node> vars_s, final List<Node> vars_p, final List<Node> vars_o, final double J_Tu) {
        final int unusualjoins = countDuplicates(vars_s,vars_p) + countDuplicates(vars_p,vars_o)+ countDuplicates(vars_p);
        return unusualjoins * J_Tu;
    }

    private static int countDuplicates( final List<Node> vars ) {
        final Map<Node,Integer> histogram= new HashMap<>();
        vars.forEach(s-> histogram.put(s, (histogram.containsKey(s) ? histogram.get(s) : -1) +1)
        );
        final AtomicInteger dups= new AtomicInteger(0);
        histogram.keySet().forEach(s -> {
            if(histogram.get(s) > 0)  dups.set(dups.get() + histogram.get(s));
        });
        return dups.get();
    }

    private static int countDuplicates( final List<Node> vars_a, final List<Node> vars_b ) {
        final Map<Node,Integer> histogram= new HashMap<>();
        vars_a.forEach(s-> histogram.put(s, (histogram.containsKey(s) ? histogram.get(s) : 0) +1)
        );
        final AtomicInteger dups= new AtomicInteger(0);
        vars_b.forEach(b -> {
            if(histogram.containsKey(b) && histogram.get(b)>0) {
                dups.incrementAndGet();
                histogram.put(b, histogram.get(b)-1);
            }
        });
        return dups.get();
    }

}
