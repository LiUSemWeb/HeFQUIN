package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;

import java.util.List;
import java.util.Set;

public class PathMatchClause implements MatchClause {

    protected final List<PathMatchClause.Edge> edges;

    //assumes that edges[i].right==edges[i+1].left
    public PathMatchClause(List<Edge> edges) {
        this.edges = edges;
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        for (final Edge e : edges) {
            e.left.visit(visitor);
            e.edge.visit(visitor);
            e.right.visit(visitor);
        }
        visitor.visitPathMatch(this);
    }

    @Override
    public boolean isRedundantWith(final MatchClause match) {
        return false;
    }

    @Override
    public Set<CypherVar> getVars() {
        return null;
    }

    @Override
    public String toString() {
        final StringBuilder builder = new StringBuilder();
        builder.append("MATCH ");
        boolean first = true;
        for (final Edge e : edges) {
            if (first) {
                builder.append("(").append(e.left).append(")");
                first = false;
            }
            if (e.direction.equals(Direction.RIGHT2LEFT)) {
                builder.append("<");
            }
            builder.append("-").append("[").append(e.edge).append("]").append("-");
            if (e.direction.equals(Direction.LEFT2RIGHT)) {
                builder.append(">");
            }
            builder.append("(").append(e.right).append(")");
        }
        return builder.toString();
    }

    public List<Edge> getEdges() {
        return edges;
    }

    public enum Direction {LEFT2RIGHT, RIGHT2LEFT, UNDIRECTED}

    public static class Edge {
        protected final CypherVar left;
        protected final CypherVar edge;
        protected final CypherVar right;
        protected final PathMatchClause.Direction direction;

        public Edge(final CypherVar left, final CypherVar edge, final CypherVar right, final Direction direction) {
            this.left = left;
            this.edge = edge;
            this.right = right;
            this.direction = direction;
        }

        public Direction getDirection() {
            return direction;
        }
    }
}
