package se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.heuristics.utils;

import org.apache.jena.graph.Node;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Var_Analyzer {

    public static int calculateVars( final List<Node> vars, final Set<Node> varsTotal, final Set<Node> bindings) {
        varsTotal.addAll( getUniqueNodes(vars) );
        varsTotal.removeAll( bindings );
        bindings.addAll( varsTotal );
        return varsTotal.size();
    }

    public static Set<Node> getUniqueNodes( final List<Node> vars) {
        Set<Node> uvars = new HashSet<Node>(vars);
        return uvars;
    }

    public static Set<Node> addBinds( final List<Node> vars_s,
                                      final List<Node> vars_p,
                                      final List<Node> vars_o) {
        final Set<Node> tempBinds = new HashSet<>();
        tempBinds.addAll(vars_s);
        tempBinds.addAll(vars_o);
        tempBinds.addAll(vars_p);
        return tempBinds;
    }

}
