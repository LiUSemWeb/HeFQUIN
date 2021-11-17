package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Set;

public class EdgeLabelCondition implements WhereCondition {

    private final CypherVar var;
    private final String clazz;

    public EdgeLabelCondition( final CypherVar var, final String clazz ) {
        assert var!= null;
        assert clazz != null;
        this.var = var;
        this.clazz = clazz;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getClazz() {
        return clazz;
    }

    @Override
    public Set<CypherVar> getVars() {
        return null;
    }

    @Override
    public String toString() {
        return var.getName()+":"+clazz;
    }
}
