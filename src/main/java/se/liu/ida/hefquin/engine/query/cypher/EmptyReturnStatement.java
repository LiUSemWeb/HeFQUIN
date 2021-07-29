package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class EmptyReturnStatement implements ReturnStatement{
    private final String alias;

    public EmptyReturnStatement(final String alias) {
        this.alias = alias;
    }

    public String getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "\"\"" + (alias != null? " AS " + alias : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmptyReturnStatement)) return false;
        EmptyReturnStatement that = (EmptyReturnStatement) o;
        return Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }
}
