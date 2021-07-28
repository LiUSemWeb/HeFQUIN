package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class EdgeMappingReturnStatement implements ReturnStatement{
    private final CypherVar edge;
    private final String alias;

    public EdgeMappingReturnStatement(final CypherVar edge, final String alias) {
        assert edge != null;
        this.edge = edge;
        this.alias = alias;
    }

    public CypherVar getEdge() {
        return edge;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "elm(" + edge.getName() + ")" + (alias != null? " AS " + alias : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeMappingReturnStatement)) return false;
        EdgeMappingReturnStatement that = (EdgeMappingReturnStatement) o;
        return edge.equals(that.edge) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edge, alias);
    }
}
