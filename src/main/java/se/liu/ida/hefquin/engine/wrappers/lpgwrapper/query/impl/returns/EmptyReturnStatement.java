package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statements that returns an empty string, with an optional alias
 * For example, RETURN "" AS x
 */
public class EmptyReturnStatement implements ReturnStatement {
    private final CypherVar alias;

    public EmptyReturnStatement(final CypherVar alias) {
        this.alias = alias;
    }

    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String getExpression() {
        return "\"\"";
    }

    @Override
    public String toString() {
        return "\"\"" + (alias != null? " AS " + alias.getName() : "");
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
