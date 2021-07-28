package se.liu.ida.hefquin.engine.query.cypher;

import java.util.Objects;

public class ClassWhereCondition implements WhereCondition{
    private final CypherVar var;
    private final String clazz;

    public ClassWhereCondition(final CypherVar var, final String clazz) {
        assert var != null;
        assert clazz != null;
        this.var = var;
        this.clazz = clazz;
    }

    public CypherVar getVar() {
        return var;
    }

    public String getCypherClass() {
        return clazz;
    }

    @Override
    public String toString() {
        return var.getName() + ":" + clazz;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClassWhereCondition)) return false;
        ClassWhereCondition that = (ClassWhereCondition) o;
        return var.equals(that.var) && clazz.equals(that.clazz);
    }

    @Override
    public int hashCode() {
        return Objects.hash(var, clazz);
    }
}
