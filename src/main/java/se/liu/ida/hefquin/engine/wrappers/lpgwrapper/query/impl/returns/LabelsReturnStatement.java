package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns the labels of a node, with an optional alias
 * For example, RETURN labels(x)
 */
public class LabelsReturnStatement implements ReturnStatement {
    private final CypherVar node;
    private final CypherVar alias;

    public LabelsReturnStatement(final CypherVar node, final CypherVar alias) {
        assert node != null;
        this.node = node;
        this.alias = alias;
    }

    public CypherVar getNode() {
        return node;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "labels(" + node.getName() + ")" + (alias != null? " AS " + alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LabelsReturnStatement)) return false;
        LabelsReturnStatement that = (LabelsReturnStatement) o;
        return node.equals(that.node) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node, alias);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(node);
    }
}
