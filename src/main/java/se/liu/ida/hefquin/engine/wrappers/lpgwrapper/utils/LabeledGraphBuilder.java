package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.*;

public class LabeledGraphBuilder {
    protected final Map<CypherVar, List<LabeledGraph.Edge>> adjacencyList = new HashMap<>();
    protected int currentId = 0;

    public void addEdge(final CypherVar src, final CypherVar edge, final CypherVar tgt) {
        ++currentId;
        List<LabeledGraph.Edge> list = adjacencyList.get(src);
        if (list == null) {
            list = new LinkedList<>();
            adjacencyList.put(src, list);
        }
        list.add( new LabeledGraph.Edge(currentId, edge, tgt, LabeledGraph.Direction.LEFT2RIGHT) );
        list = adjacencyList.get(tgt);
        if (list == null) {
            list = new LinkedList<>();
            adjacencyList.put(tgt, list);
        }
        list.add( new LabeledGraph.Edge(currentId, edge, src, LabeledGraph.Direction.RIGHT2LEFT) );
    }

    public void addNode(final CypherVar node) {
        if (!adjacencyList.containsKey(node))
            adjacencyList.put(node, new ArrayList<>());
    }

    public LabeledGraph build() {
        return new LabeledGraph(adjacencyList);
    }
}
