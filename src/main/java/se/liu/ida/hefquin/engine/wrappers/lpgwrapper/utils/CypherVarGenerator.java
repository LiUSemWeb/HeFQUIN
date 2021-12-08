package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CypherVarGenerator {
    private final Map<String, CypherVar> innerVars = new HashMap<>();
    private final Map<String, CypherVar> retVars = new HashMap<>();
    private final Map<String, List<CypherVar>> edgeVars = new HashMap<>();

    private int varCount = 0;
    private int edgeCount = 0;
    private int anonCount = 0;
    private int retVarCount = 0;

    private static final String varPrefix = "cpvar";
    private static final String retPrefix = "ret";
    private static final String anonPrefix = "a";

    public CypherVar getVarFor(final Node n) {
        CypherVar var = innerVars.get(n.getName());
        if (var != null)
            return var;
        varCount++;
        var = new CypherVar(varPrefix + varCount);
        innerVars.put(n.getName(), var);
        return var;
    }

    public List<CypherVar> getEdgeVars(final Node n) {
        List<CypherVar> var = edgeVars.get(n.getName());
        if (var != null)
            return var;
        edgeCount++;
        final CypherVar source = new CypherVar("src" + edgeCount);
        final CypherVar edge = new CypherVar("edge" + edgeCount);
        final CypherVar target = new CypherVar("tgt" + edgeCount);
        final List<CypherVar> vars = new ArrayList<>(3);
        vars.add(source);
        vars.add(edge);
        vars.add(target);
        edgeVars.put(n.getName(), vars);
        return vars;
    }

    public CypherVar getAnonVar() {
        anonCount++;
        return new CypherVar(anonPrefix + anonCount);
    }

    public CypherVar getRetVar(final Node n) {
        CypherVar var = retVars.get(n.getName());
        if (var != null)
            return var;
        retVarCount++;
        var = new CypherVar(retPrefix + retVarCount);
        retVars.put(n.getName(), var);
        return var;
    }

    public String getReverseRetVarName(final CypherVar var) {
        for (final Map.Entry<String, CypherVar> entry : retVars.entrySet()) {
            if (entry.getValue().equals(var)) {
                return entry.getKey();
            }
        }
        return null;
    }
}
