package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a Cypher condition on the label of a node.
 * For example, n:Person
 */
public class NodeLabelCondition implements WhereCondition {

    private final CypherVar var;
    private final String label;

    public NodeLabelCondition(final CypherVar var, final String label) {
        assert var != null;
        assert label != null;
        this.var = var;
        this.label = label;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getCypherClass() {
        return label;
    }

    @Override
    public String toString() {
        return var.getName() + ":" + label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeLabelCondition)) return false;
        NodeLabelCondition that = (NodeLabelCondition) o;
        return var.equals(that.var) && label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, label);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
