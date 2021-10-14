package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class NodeMappingReturnStatement implements ReturnStatement{
    private final CypherVar node;
    private final String alias;

    public NodeMappingReturnStatement(final CypherVar node, final String alias) {
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
        return "nm(" + node.getName() + ")" + (alias != null? " AS "+alias : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeMappingReturnStatement)) return false;
        NodeMappingReturnStatement that = (NodeMappingReturnStatement) o;
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
