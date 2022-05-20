package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;

public class GraphCycleDetector {

    /**
     * Helper function used to call the DFS function for each SgpNode
     */
    public static Set<Integer> determineCyclicConnectors(final Map<Node,SGPNode> sgpNodes){
        final Set<Integer> connectorsToBeRemoved = new HashSet<>();

        for(final SGPNode n : sgpNodes.values()){
            DFS(n,connectorsToBeRemoved);
        }

        return connectorsToBeRemoved;
    }

    /**
     * DFS algorithm function to determine which connectors are to be removed later
     */
    protected static boolean DFS(final SGPNode node, final Set<Integer> removeConnectors){
        if(node.isFinished()){
            return false;
        }
        if(node.isVisited()){
            return true;
        }
        node.setVisited(true);
        for(final int connectorId : node.getAdjacentNodes().keySet()){
            final boolean visited = DFS(node.getAdjacentNodes().get(connectorId), removeConnectors);

            if(visited){
                removeConnectors.add(connectorId);
            }
        }

        node.setFinished(true);

        return false;
    }
}
