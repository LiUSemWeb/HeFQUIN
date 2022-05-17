package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class representing the nodes used to check for cyclic bindings between SGPs
 */
public class SGPNode {
    private final Map<Integer,SGPNode> adjacentNodes = new HashMap<>();
    private boolean visited = false;
    private boolean finished = false;

    public boolean isVisited(){
        return visited;
    }

    public boolean isFinished(){
        return finished;
    }

    public void setVisited(final boolean value){
        visited = value;
    }

    public void setFinished(final boolean value){
        finished = value;
    }

    public void addAdjacentNode(final int connectorID, final SGPNode sgpNode){
        adjacentNodes.put(connectorID, sgpNode);
    }

    public Map<Integer,SGPNode> getAdjacentNodes(){
        return adjacentNodes;
    }
}
