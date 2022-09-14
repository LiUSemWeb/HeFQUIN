package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Objects;
import java.util.Set;

public class EdgeLabelCondition implements WhereCondition {

    private final CypherVar var;
    private final String label;

    public EdgeLabelCondition( final CypherVar var, final String label ) {
        assert var!= null;
        assert label != null;
        this.var = var;
        this.label = label;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public Set<CypherVar> getVars() {
        return null;
    }

    @Override
    public String toString() {
        return var.getName()+":"+ label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EdgeLabelCondition)) return false;
        EdgeLabelCondition that = (EdgeLabelCondition) o;
        return var.equals(that.var) && label.equals(that.label);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, label);
    }
}
