package se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.match;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherExpressionVisitor;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.LabeledGraph;

public class PathMatchClause implements MatchClause {

    protected final List<EdgePattern> edges;

    //assumes that edges[i].right==edges[i+1].left
    public PathMatchClause(final List<EdgePattern> edges) {
        this.edges = edges;
    }

    public PathMatchClause(final LabeledGraph.Path path) {
        edges = new ArrayList<>();
        CypherVar start = path.getStart();
        for (final LabeledGraph.Edge e : path.getEdges()) {
            edges.add(new EdgePattern(start, e.getEdge(), e.getTarget(), e.getDirection()));
            start = e.getTarget();
        }
    }

    @Override
    public void visit(final CypherExpressionVisitor visitor) {
        for (final EdgePattern e : edges) {
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
        for (final EdgePattern e : edges) {
            if (first) {
                builder.append("(").append(e.left).append(")");
                first = false;
            }
            if (e.direction.equals(LabeledGraph.Direction.RIGHT2LEFT)) {
                builder.append("<");
            }
            builder.append("-").append("[").append(e.edge).append("]").append("-");
            if (e.direction.equals(LabeledGraph.Direction.LEFT2RIGHT)) {
                builder.append(">");
            }
            builder.append("(").append(e.right).append(")");
        }
        return builder.toString();
    }

    public List<EdgePattern> getEdges() {
        return edges;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PathMatchClause that = (PathMatchClause) o;
        return edges.equals(that.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(edges);
    }

    public static class EdgePattern {
        public final CypherVar left;
        public final CypherVar edge;
        public final CypherVar right;
        public final LabeledGraph.Direction direction;

        public EdgePattern(final CypherVar left, final CypherVar edge, final CypherVar right, final LabeledGraph.Direction direction) {
            assert left != null;
            assert edge != null;
            assert right != null;
            assert direction != null;

            this.left = left;
            this.edge = edge;
            this.right = right;
            this.direction = direction;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EdgePattern that = (EdgePattern) o;
            return left.equals(that.left) && edge.equals(that.edge) && right.equals(that.right) && direction == that.direction;
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, edge, right, direction);
        }
    }
}
