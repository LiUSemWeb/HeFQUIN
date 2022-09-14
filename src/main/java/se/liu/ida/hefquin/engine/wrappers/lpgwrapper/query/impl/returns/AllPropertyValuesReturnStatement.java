package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns the values of all properties of an edge or node, with an optional alias
 * For example, RETURN [k IN KEYS(x) | x[k]]
 */
public class AllPropertyValuesReturnStatement implements ReturnStatement {
    private final CypherVar var;
    private final CypherVar alias;
    private final String innerVar = "k";

    public AllPropertyValuesReturnStatement(final CypherVar var, final CypherVar alias) {
        assert var != null;
        this.var = var;
        this.alias = alias;
    }

    public CypherVar getVar() {
        return var;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String toString() {
        return "[" + innerVar + " IN KEYS(" + var.getName() + ") | " + var.getName() + "[" + innerVar + "]]"
                +(alias != null ? " AS " + alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof AllPropertyValuesReturnStatement)) return false;
        AllPropertyValuesReturnStatement that = (AllPropertyValuesReturnStatement) o;
        return var.equals(that.var) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, alias, innerVar);
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }
}
