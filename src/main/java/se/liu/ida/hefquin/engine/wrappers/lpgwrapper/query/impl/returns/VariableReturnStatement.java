package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a statement that returns a the bindings of a Cypher variable, with an optional alias
 * For example, RETURN n AS s
 */
public class VariableReturnStatement implements ReturnStatement {

    protected final CypherVar returnVar;
    protected final CypherVar alias;

    public VariableReturnStatement(final CypherVar returnVar) {
        this(returnVar, null);
    }

    public VariableReturnStatement(final CypherVar returnVar, final CypherVar alias) {
        assert returnVar != null;
        this.returnVar = returnVar;
        this.alias = alias;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(returnVar);
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String getExpression() {
        return returnVar.getName();
    }

    @Override
    public String toString() {
        return returnVar.getName() + ( alias != null? " AS "+ alias.getName() : "");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VariableReturnStatement)) return false;
        VariableReturnStatement that = (VariableReturnStatement) o;
        return returnVar.equals(that.returnVar) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(returnVar, alias);
    }
}
