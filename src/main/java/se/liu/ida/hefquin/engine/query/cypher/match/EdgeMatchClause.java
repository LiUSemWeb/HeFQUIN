package se.liu.ida.hefquin.engine.query.cypher.match;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a directed path match statement
 * For example MATCH (x)-[e:label]->(y)
 */
public class EdgeMatchClause implements MatchClause {
    protected final CypherVar sourceNode;
    protected final CypherVar targetNode;
    protected final CypherVar edge;
    protected final String edgeLabel;

    public EdgeMatchClause(final CypherVar sourceNode, final CypherVar targetNode, final CypherVar edge,
                           final String edgeLabel) {
        assert sourceNode != null;
        assert targetNode != null;
        this.sourceNode = sourceNode;
        this.targetNode = targetNode;
        this.edge = edge;
        this.edgeLabel = edgeLabel;
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

    public String getEdgeLabel() {
        return edgeLabel;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MATCH (")
                .append(sourceNode.getName())
                .append(")-[");
        if (edge != null)
            builder.append(edge.getName());
        if (edgeLabel != null){
            builder.append(":").append(edgeLabel);
        }
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
                && Objects.equals(edge, that.edge) && Objects.equals(edgeLabel, that.edgeLabel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sourceNode, targetNode, edge, edgeLabel);
    }

    @Override
    public boolean isRedundantWith(final MatchClause match) {
        if (match instanceof NodeMatchClause) {
            final NodeMatchClause that = (NodeMatchClause) match;
            return sourceNode.equals(that.node) || targetNode.equals(that.node);
        }
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
