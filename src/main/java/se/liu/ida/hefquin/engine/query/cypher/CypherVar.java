package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class CypherVar {
    private final String name;

    public CypherVar(final String name) {
        assert name != null;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CypherVar)) return false;
        CypherVar cypherVar = (CypherVar) o;
        return name.equals(cypherVar.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
