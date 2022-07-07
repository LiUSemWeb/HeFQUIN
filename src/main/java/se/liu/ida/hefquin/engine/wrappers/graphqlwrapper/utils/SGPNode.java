package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.HashMap;
import java.util.Map;

import se.liu.ida.hefquin.engine.query.TriplePattern;

/**
 * Helper class representing the nodes used to check for cyclic bindings between SGPs
 */
public class SGPNode {
    private final Map<TriplePattern,SGPNode> adjacentNodes = new HashMap<>();
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

    public void addAdjacentNode(final TriplePattern tp, final SGPNode sgpNode){
        adjacentNodes.put(tp, sgpNode);
    }

    public Map<TriplePattern,SGPNode> getAdjacentNodes(){
        return adjacentNodes;
    }
}
