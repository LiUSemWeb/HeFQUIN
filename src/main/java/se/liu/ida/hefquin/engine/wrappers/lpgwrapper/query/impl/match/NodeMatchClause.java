package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.MatchClause;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a node match clause
 * For example, MATCH (x)
 */
public class NodeMatchClause implements MatchClause {

    protected final CypherVar node;

    public NodeMatchClause(final CypherVar node) {
        assert node != null;
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
        return this.equals(match);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(node);
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

    public CypherVar getNode() {
        return node;
    }
}
