package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.HashMap;
import java.util.Map;

public class CypherVarGenerator {
    private final Map<Var, CypherVar> retVars = new HashMap<>();
    private final Map<CypherVar, Var> reverseRetVars = new HashMap<>();
    private int anonCount = 0;
    private int retVarCount = 0;

    private static final String retPrefix = "ret";
    private static final String anonPrefix = "a";
    private static final String markerPrefix = "m";

    public CypherVar getAnonVar() {
        anonCount++;
        return new CypherVar(anonPrefix + anonCount);
    }

    public CypherVar getRetVar(final Node n) {
        if (!n.isVariable()) throw new IllegalArgumentException("Expected variable, got: " + n.getClass());
        CypherVar var = retVars.get(n);
        if (var != null)
            return var;
        retVarCount++;
        var = new CypherVar(retPrefix + retVarCount);
        retVars.put((Var) n, var);
        reverseRetVars.put(var, (Var) n);
        return var;
    }

    public CypherVar getMarkerVar() {
        return new CypherVar(markerPrefix);
    }

    public Map<CypherVar, Var> getReverseMap() {
        return reverseRetVars;
    }
}
