package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Objects;
import java.util.Set;

public class VariableGetItemReturnStatement implements ReturnStatement {

    protected final CypherVar var;
    protected final int i;
    protected final CypherVar alias;


    public VariableGetItemReturnStatement(final CypherVar var, final int i, final CypherVar alias) {
        assert var != null;
        assert i >= 0;
        this.var = var;
        this.i = i;
        this.alias = alias;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Set.of(var, alias);
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VariableGetItemReturnStatement that = (VariableGetItemReturnStatement) o;
        return i == that.i && var.equals(that.var) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, i, alias);
    }

    @Override
    public String toString() {
        return var+"["+i+"]" + (alias==null? "" : " AS "+alias);
    }
}
