package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class CountLargerThanZeroReturnStatement implements ReturnStatement {

    protected final CypherVar alias;

    public CountLargerThanZeroReturnStatement(final CypherVar alias) {
        assert alias != null;
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CountLargerThanZeroReturnStatement that = (CountLargerThanZeroReturnStatement) o;
        return alias.equals(that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(alias);
    }

    @Override
    public String toString() {
        return "COUNT(*) > 0 AS " + alias;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(alias);
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }
}
