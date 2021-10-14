package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class IDWhereCondition implements WhereCondition{

    private final CypherVar var;
    private final String nodeID;

    public IDWhereCondition(CypherVar var, String nodeID) {
        assert var!= null;
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
        if (!(o instanceof IDWhereCondition)) return false;
        IDWhereCondition that = (IDWhereCondition) o;
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
