package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.*;
import java.util.stream.Collectors;

public class LabeledGraph {

    public enum Direction {LEFT2RIGHT, RIGHT2LEFT, UNDIRECTED}

    public static class Edge {
        protected final int id;
        protected final CypherVar edge;
        protected final CypherVar target;
        protected final Direction direction;

        public Edge(final int id, final CypherVar edge, final CypherVar target, final Direction direction) {
            this.id = id;
            this.edge = edge;
            this.target = target;
            this.direction = direction;
        }

        @Override
        public String toString() {
            return "(" + id +
                    ", " + edge +
                    ", " + target +
                    ", " + direction +
                    ')';
        }
    }

    protected final Map<CypherVar, List<Edge>> adjacencyLists;

    public LabeledGraph(final Map<CypherVar, List<Edge>> adjacencyLists) {
        assert adjacencyLists != null;
        this.adjacencyLists = adjacencyLists;
    }

    /**
     * removes the edges contained in path
     */
    public void removePath(final Path path) {
        CypherVar current = path.start;
        for (final Edge e : path.path) {
            //removes LEFT2RIGHT
            List<Edge> toRemove = adjacencyLists.get(current).stream().filter(x->x.edge.equals(e.edge)).collect(Collectors.toList());
            adjacencyLists.get(current).removeAll(toRemove);
            if (adjacencyLists.get(current).isEmpty()) {
                adjacencyLists.remove(current);
            }
            //removes RIGHT2LEFT
            if (!adjacencyLists.containsKey(e.target)) continue;;
            toRemove = adjacencyLists.get(e.target).stream().filter(x->x.id==e.id).collect(Collectors.toList());
            adjacencyLists.get(e.target).removeAll(toRemove);
            if (adjacencyLists.get(e.target).isEmpty()) {
                adjacencyLists.remove(e.target);
            }
            current = e.target;
        }
    }

    public Path getLongestPath() {
        Path longest = null;
        for (final CypherVar start : adjacencyLists.keySet()) {
           final Path candidate = longestStartingFrom(start);
           if (longest == null || candidate.size() > longest.size()) {
               longest = candidate;
           }
        }
        return longest;
    }

    private Path longestStartingFrom(final CypherVar start) {
        System.out.println("start: "+start);
        final Set<Integer> visitedEdges = new HashSet<>();
        final Deque<Edge> toVisit = new ArrayDeque<>();
        for (final Edge e : adjacencyLists.get(start)) {
            toVisit.push(e);
        }
        Path candidate = null;
        Path longest = null;
        while (!toVisit.isEmpty()) {
            final Edge currentEdge = toVisit.pop();
            System.out.println("current edge: " + currentEdge);
            if (candidate == null) {
                candidate = new Path(start, currentEdge);
            }
            else if (! visitedEdges.contains(currentEdge.id)) {
                candidate.addEdge(currentEdge);
            }
            visitedEdges.add(currentEdge.id);
            System.out.println("current path: " + candidate);
            if (!allVisited(adjacencyLists.get(currentEdge.target), visitedEdges)) {
                System.out.println("going through: " + currentEdge.target);
                System.out.println(visitedEdges);
                System.out.println(adjacencyLists.get(currentEdge.target));
                for (final Edge e : adjacencyLists.get(currentEdge.target)) {
                    if (!visitedEdges.contains(e.id)) {
                        toVisit.push(e);
                    }
                }
            } else {
                if ( longest == null || candidate.size() > longest.size() ) {
                    longest = candidate.copy();
                }
                System.out.println("end of path");
                candidate.removeLast();
            }
        }
        return longest;
    }

    private boolean allVisited(final List<Edge> edges, Set<Integer> visitedEdges) {
        return edges == null || visitedEdges.containsAll(edges.stream().map(x->x.id).collect(Collectors.toSet()));
    }

    @Override
    public String toString() {
        return adjacencyLists.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LabeledGraph graph = (LabeledGraph) o;
        return adjacencyLists.equals(graph.adjacencyLists);
    }

    @Override
    public int hashCode() {
        return Objects.hash(adjacencyLists);
    }

    public static class Path {
        protected CypherVar start;
        protected List<Edge> path;

        public Path(final CypherVar start, final Edge e) {
            this.start = start;
            this.path = new LinkedList<>();
            this.path.add(e);
        }

        public Path(final CypherVar start, final List<Edge> path) {
            this.start = start;
            this.path = path;
        }

        public void addEdge(final Edge e) {
            this.path.add(e);
        }

        public int size() {
            return path.size();
        }

        @Override
        public String toString() {
            final StringBuilder builder = new StringBuilder();
            builder.append("(").append(start).append(")");
            for (final LabeledGraph.Edge e : path) {
                if (e.direction.equals(LabeledGraph.Direction.RIGHT2LEFT)) {
                    builder.append("<");
                }
                builder.append("-").append("[").append(e.edge).append("]").append("-");
                if (e.direction.equals(LabeledGraph.Direction.LEFT2RIGHT)) {
                    builder.append(">");
                }
                builder.append("(").append(e.target).append(")");
            }
            return builder.toString();
        }

        public void removeLast() {
            this.path.remove(size()-1);
        }

        public Path copy() {
            return new Path(start, new LinkedList<>(path));
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Path path1 = (Path) o;
            return start.equals(path1.start) && path.equals(path1.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(start, path);
        }
    }

}
