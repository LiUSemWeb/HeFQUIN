package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class PropertyValueConditionWithVar implements WhereCondition {

    protected final CypherVar var;
    protected final CypherVar propVar;
    protected final String literal;

    public PropertyValueConditionWithVar(final CypherVar var, final CypherVar propVar, final String literal) {
        assert var != null;
        assert propVar != null;
        assert literal != null;
        this.var = var;
        this.propVar = propVar;
        this.literal = literal;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(var);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PropertyValueConditionWithVar that = (PropertyValueConditionWithVar) o;
        return var.equals(that.var) && propVar.equals(that.propVar) && literal.equals(that.literal);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, propVar, literal);
    }

    @Override
    public String toString() {
        return var+"["+propVar+"]='"+literal+"'";
    }

}
