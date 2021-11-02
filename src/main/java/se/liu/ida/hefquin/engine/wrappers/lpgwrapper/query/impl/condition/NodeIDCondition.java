package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a condition checking the identifier of a node
 * For example, ID(n)=22
 */
public class NodeIDCondition implements WhereCondition {

    private final CypherVar var;
    private final String nodeID;

    public NodeIDCondition( final CypherVar var, final String nodeID ) {
        assert var!= null;
        assert nodeID != null;
        this.var = var;
        this.nodeID = nodeID;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getNodeID() {
        return nodeID;
    }

    @Override
    public String toString() {
        return "ID("+var.getName()+")="+nodeID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeIDCondition)) return false;
        NodeIDCondition that = (NodeIDCondition) o;
        return nodeID.equals(that.nodeID) && var.equals(that.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, nodeID);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
