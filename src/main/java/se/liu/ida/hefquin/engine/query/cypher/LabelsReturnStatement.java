package se.liu.ida.hefquin.engine.query.cypher;

public class LabelsReturnStatement {
    private final CypherVar node;
    private final String alias;

    public LabelsReturnStatement(final CypherVar node, final String alias) {
        assert node != null;
        this.node = node;
        this.alias = alias;
    }

    public CypherVar getNode() {
        return node;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "labels(" + node.getName() + ")" + (alias != null? " AS " + alias : "");
    }
}
