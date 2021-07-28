package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class NodeMatchClause implements MatchClause{

    protected final CypherVar node;

    public NodeMatchClause(final CypherVar node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "MATCH ("+node.getName()+")";
    }

    @Override
    public boolean isRedundantWith(final MatchClause match) {
        if (match instanceof EdgeMatchClause) {
            final EdgeMatchClause that = (EdgeMatchClause) match;
            return node.equals(that.sourceNode) || node.equals(that.targetNode);
        }
        return false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeMatchClause)) return false;
        NodeMatchClause that = (NodeMatchClause) o;
        return node.equals(that.node);
    }

    @Override
    public int hashCode() {
        return Objects.hash(node);
    }
}
