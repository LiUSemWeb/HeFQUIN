package se.liu.ida.hefquin.engine.query.cypher.returns;

import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;

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
    private final String innerVar;

    public AllPropertyValuesReturnStatement(final CypherVar var, final CypherVar alias) {
        this(var, alias, "k");
    }

    public AllPropertyValuesReturnStatement(final CypherVar var, final CypherVar alias, final String innerVar) {
        assert var != null;
        this.var = var;
        this.alias = alias;
        this.innerVar = innerVar;
    }

    public CypherVar getVar() {
        return var;
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    public String getInnerVar() {
        return innerVar;
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
        return var.equals(that.var) && Objects.equals(alias, that.alias) && innerVar.equals(that.innerVar);
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
