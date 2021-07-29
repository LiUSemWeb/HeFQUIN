package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class LabelsReturnStatement implements ReturnStatement{
    private final CypherVar node;
    private final String alias;

    public LabelsReturnStatement(final CypherVar node, final String alias) {
        assert node != null;
        this.node = node;
        this.alias = alias;
    }

    public CypherVar getNode() {
        return node;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "labels(" + node.getName() + ")" + (alias != null? " AS " + alias : "");
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
