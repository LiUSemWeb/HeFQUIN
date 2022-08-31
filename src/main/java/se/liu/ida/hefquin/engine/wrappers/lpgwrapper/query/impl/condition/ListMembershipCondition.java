package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class ListMembershipCondition implements WhereCondition {

    private final CypherVar testVar;
    private final String listExpression;

    public ListMembershipCondition(final CypherVar testVar, final String listExpression) {
        assert testVar != null;
        assert listExpression != null;
        this.testVar = testVar;
        this.listExpression = listExpression;
    }

    @Override
    public Set<CypherVar> getVars() {
        //TODO: listExpression should be a CypherExpression, its variables should be added to the returned set
        return Collections.singleton(testVar);
    }

    @Override
    public String toString() {
        return testVar + " IN " + listExpression;
    }

    @Override
    public boolean equals(Object o) {
        if(this==o) return true;
        if(! (o instanceof ListMembershipCondition)) return false;
        final ListMembershipCondition that = (ListMembershipCondition) o;
        return this.listExpression.equals(that.listExpression) && this.testVar.equals(that.testVar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(testVar, listExpression);
    }
}
