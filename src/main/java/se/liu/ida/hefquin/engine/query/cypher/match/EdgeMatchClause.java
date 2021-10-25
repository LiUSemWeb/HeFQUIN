package se.liu.ida.hefquin.engine.query.cypher.match;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a directed path match statement. Conditions on the labels of the nodes or edges, as well as
 * over property values of nodes or edges are managed through the WHERE clause.
 * For example MATCH (x)-[e]->(y)
 */
public class EdgeMatchClause implements MatchClause {
    protected final CypherVar sourceNode;
    protected final CypherVar targetNode;
    protected final CypherVar edge;

    public EdgeMatchClause(final CypherVar sourceNode, final CypherVar edge, final CypherVar targetNode) {
        assert sourceNode != null;
        assert edge != null;
        assert targetNode != null;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.edge = edge;
    }

    public CypherVar getSourceNode() {
        return sourceNode;
    }

    public CypherVar getTargetNode() {
        return targetNode;
    }

    public CypherVar getEdge() {
        return edge;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MATCH (")
                .append(sourceNode.getName())
                .append(")-[");
        if (edge != null)
            builder.append(edge.getName());
        builder.append("]->(")
                .append(targetNode.getName())
                .append(")");
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeMatchClause)) return false;
        EdgeMatchClause that = (EdgeMatchClause) o;
        return sourceNode.equals(that.sourceNode) && targetNode.equals(that.targetNode)
                && Objects.equals(edge, that.edge);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceNode, targetNode, edge);
    }

    @Override
    public boolean isRedundantWith(final MatchClause match) {
        return this.equals(match);
    }

    @Override
    public Set<CypherVar> getVars() {
        final Set<CypherVar> vars = new HashSet<>();
        vars.add(sourceNode);
        vars.add(targetNode);
        if (edge != null) vars.add(edge);
        return vars;
    }
}
