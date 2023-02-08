package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.junit.Test;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.LabeledGraph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class LabeledGraphTest {

    final CypherVar a = new CypherVar("a");
    final CypherVar b = new CypherVar("b");
    final CypherVar c = new CypherVar("c");
    final CypherVar d = new CypherVar("d");
    final CypherVar e = new CypherVar("e");
    final CypherVar x = new CypherVar("x");
    final CypherVar y = new CypherVar("y");
    final CypherVar z = new CypherVar("z");
    final CypherVar u = new CypherVar("u");
    final CypherVar w = new CypherVar("w");
    final CypherVar t = new CypherVar("t");

    @Test
    public void longestPathTest() {
        final Map<CypherVar, List<LabeledGraph.Edge>> adjacencyList = new HashMap<>();
        final LabeledGraph.Edge tewr2l = new LabeledGraph.Edge(5, e, w, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge wczl2r = new LabeledGraph.Edge(3, c, z, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge zbyr2l = new LabeledGraph.Edge(2, b, y, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge yaxr2l = new LabeledGraph.Edge(1, a, x, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge xayl2r = new LabeledGraph.Edge(1, a, y, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge ybzl2r = new LabeledGraph.Edge(2, b, z, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge zcwr2l = new LabeledGraph.Edge(3, c, w, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge wdul2r = new LabeledGraph.Edge(4, d, u, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge wetl2r = new LabeledGraph.Edge(5, e, t, LabeledGraph.Direction.LEFT2RIGHT);
        adjacencyList.put(x, List.of(xayl2r));
        adjacencyList.put(y, List.of(yaxr2l, ybzl2r));
        adjacencyList.put(z, List.of(zbyr2l, zcwr2l));
        adjacencyList.put(w, List.of(wczl2r, wdul2r, wetl2r));
        adjacencyList.put(u, List.of(new LabeledGraph.Edge(4, d, w, LabeledGraph.Direction.RIGHT2LEFT)));
        adjacencyList.put(t, List.of(tewr2l));
        final LabeledGraph graph = new LabeledGraph(adjacencyList);
        assertEquals(new LabeledGraph.Path(x, List.of(xayl2r, ybzl2r, zcwr2l, wetl2r)),
                graph.getLongestPath());
    }

    @Test
    public void longestPathWithCyclesTest() {
        final Map<CypherVar, List<LabeledGraph.Edge>> adjacencyList = new HashMap<>();
        final LabeledGraph.Edge waxl2r = new LabeledGraph.Edge(1, a, x, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge xbdr2l = new LabeledGraph.Edge(2, b, y, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge ydzr2l = new LabeledGraph.Edge(3, d, z, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge zcwl2r = new LabeledGraph.Edge(4, c, w, LabeledGraph.Direction.LEFT2RIGHT);
        adjacencyList.put(x, List.of(new LabeledGraph.Edge(1, a, w, LabeledGraph.Direction.RIGHT2LEFT), xbdr2l));
        adjacencyList.put(y, List.of(new LabeledGraph.Edge(2, b, x, LabeledGraph.Direction.LEFT2RIGHT), ydzr2l));
        adjacencyList.put(z, List.of(new LabeledGraph.Edge(3, d, y, LabeledGraph.Direction.LEFT2RIGHT), zcwl2r));
        adjacencyList.put(w, List.of(waxl2r, new LabeledGraph.Edge(4, c, z, LabeledGraph.Direction.RIGHT2LEFT)));
        final LabeledGraph graph = new LabeledGraph(adjacencyList);
        assertEquals(new LabeledGraph.Path(w, List.of(waxl2r, xbdr2l, ydzr2l, zcwl2r)), graph.getLongestPath());
    }

    @Test
    public void removePathTest() {
        final Map<CypherVar, List<LabeledGraph.Edge>> adjacencyList = new HashMap<>();
        final LabeledGraph.Edge wdxl2r = new LabeledGraph.Edge(3, d, x, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge xbyl2r = new LabeledGraph.Edge(1, b, y, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge xczl2r = new LabeledGraph.Edge(2, c, z, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge xaur2l = new LabeledGraph.Edge(4, a, u, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge uaxl2r = new LabeledGraph.Edge(4, a, x, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge zcxr2l = new LabeledGraph.Edge(2, c, x, LabeledGraph.Direction.RIGHT2LEFT);
        adjacencyList.put(x, new ArrayList<>(List.of(xbyl2r, xczl2r, new LabeledGraph.Edge(3, d, w, LabeledGraph.Direction.RIGHT2LEFT), xaur2l)));
        adjacencyList.put(y, new ArrayList<>(List.of(new LabeledGraph.Edge(1, b, x, LabeledGraph.Direction.RIGHT2LEFT))));
        adjacencyList.put(z, new ArrayList<>(List.of(zcxr2l)));
        adjacencyList.put(w, new ArrayList<>(List.of(wdxl2r)));
        adjacencyList.put(u, new ArrayList<>(List.of(uaxl2r)));
        final LabeledGraph graph = new LabeledGraph(adjacencyList);
        final LabeledGraph.Path toRemove = new LabeledGraph.Path(w, List.of(wdxl2r, xbyl2r));
        graph.removePath(toRemove);

        final Map<CypherVar, List<LabeledGraph.Edge>> updatedAdjacencyList = new HashMap<>();
        updatedAdjacencyList.put(u, List.of(uaxl2r));
        updatedAdjacencyList.put(x, List.of(xczl2r, xaur2l));
        updatedAdjacencyList.put(z, List.of(zcxr2l));

        assertEquals(new LabeledGraph(updatedAdjacencyList), graph);
    }

    @Test
    public void goBack2Test() {
        final Map<CypherVar, List<LabeledGraph.Edge>> adjacencyList = new HashMap<>();
        final LabeledGraph.Edge xayl2r = new LabeledGraph.Edge(1, a, y, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge ybzl2r = new LabeledGraph.Edge(2, b, z, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge zcul2r = new LabeledGraph.Edge(3, c, u, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge ydwl2r = new LabeledGraph.Edge(4, d, w, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge wetl2r = new LabeledGraph.Edge(5, e, t, LabeledGraph.Direction.LEFT2RIGHT);
        final LabeledGraph.Edge yaxr2l = new LabeledGraph.Edge(1, a, x, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge zbyr2l = new LabeledGraph.Edge(2, b, y, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge uczr2l = new LabeledGraph.Edge(3, c, z, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge wdyr2l = new LabeledGraph.Edge(4, d, y, LabeledGraph.Direction.RIGHT2LEFT);
        final LabeledGraph.Edge tewr2l = new LabeledGraph.Edge(5, e, w, LabeledGraph.Direction.RIGHT2LEFT);
        adjacencyList.put(x, List.of(xayl2r));
        adjacencyList.put(y, List.of(ybzl2r, ydwl2r, yaxr2l));
        adjacencyList.put(z, List.of(zcul2r, zbyr2l));
        adjacencyList.put(w, List.of(wetl2r, wdyr2l));
        adjacencyList.put(u, List.of(uczr2l));
        adjacencyList.put(t, List.of(tewr2l));
        final LabeledGraph graph = new LabeledGraph(adjacencyList);
        System.out.println(graph.getLongestPath());
    }

}
