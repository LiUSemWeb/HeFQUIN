package se.liu.ida.hefquin.engine.query.cypher.returns;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns a literal, with an optional alias
 * For example, RETURN 2 AS p
 */
public class LiteralValueReturnStatement implements ReturnStatement {
    protected final String value;
    protected final CypherVar alias;

    public LiteralValueReturnStatement(final String value, final CypherVar alias) {
        assert value != null;
        this.value = value;
        this.alias = alias;
    }

    public String getValue() {
        return value;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return value + (alias != null? " AS " + alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LiteralValueReturnStatement)) return false;
        LiteralValueReturnStatement that = (LiteralValueReturnStatement) o;
        return value.equals(that.value) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, alias);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.emptySet();
    }
}
