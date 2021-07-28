package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class IDWhereCondition {

    private final CypherVar var;
    private final int nodeID;

    public IDWhereCondition(CypherVar var, int nodeID) {
        assert var!= null;
        this.var = var;
        this.nodeID = nodeID;
    }

    public CypherVar getVar() {
        return var;
    }

    public int getNodeID() {
        return nodeID;
    }

    @Override
    public String toString() {
        return "ID("+var.getName()+")="+nodeID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IDWhereCondition)) return false;
        IDWhereCondition that = (IDWhereCondition) o;
        return nodeID == that.nodeID && var.equals(that.var);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, nodeID);
    }
}
