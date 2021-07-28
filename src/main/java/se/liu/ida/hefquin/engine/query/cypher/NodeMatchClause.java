package se.liu.ida.hefquin.engine.query.cypher;

public class NodeMatchClause implements MatchClause{

    protected final CypherVar node;

    public NodeMatchClause(final CypherVar node) {
        this.node = node;
    }

    @Override
    public String toString() {
        return "MATCH ("+node.getName()+")";
    }
}
