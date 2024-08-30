package se.liu.ida.hefquin.engine.wrappers.graphql.utils;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.base.query.TriplePattern;

public class GraphCycleDetector {

    /**
     * Helper function used to call the DFS function for each SgpNode
     */
    public static Set<TriplePattern> determineCyclicConnectors(final Iterable<SGPNode> sgpNodes){
        final Set<TriplePattern> connectorsToBeRemoved = new HashSet<>();

        for(final SGPNode n : sgpNodes){
            DFS(n,connectorsToBeRemoved);
        }

        return connectorsToBeRemoved;
    }

    /**
     * DFS algorithm function to determine which connectors are to be removed later
     */
    protected static boolean DFS(final SGPNode node, final Set<TriplePattern> removeConnectors){
        if(node.isFinished()){
            return false;
        }
        if(node.isVisited()){
            return true;
        }
        node.setVisited(true);
        for(final TriplePattern connector : node.getAdjacentNodes().keySet()){
            final boolean visited = DFS(node.getAdjacentNodes().get(connector), removeConnectors);

            if(visited){
                removeConnectors.add(connector);
            }
        }

        node.setFinished(true);

        return false;
    }
}
