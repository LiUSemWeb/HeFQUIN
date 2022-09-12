package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.returns;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class RelationshipTypeReturnStatement implements ReturnStatement {

    protected final CypherVar relationshipVar;
    protected final CypherVar alias;

    public RelationshipTypeReturnStatement(final CypherVar relationshipVar) {
        this(relationshipVar, null);
    }

    public RelationshipTypeReturnStatement(final CypherVar relationshipVar, final CypherVar alias) {
        assert relationshipVar != null;
        this.relationshipVar = relationshipVar;
        this.alias = alias;
    }

    @Override
    public Set<CypherVar> getVars() {
        return Collections.singleton(relationshipVar);
    }

    @Override
    public CypherVar getAlias() {
        return alias;
    }

    @Override
    public String getExpression() {
        return "TYPE("+relationshipVar+")";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RelationshipTypeReturnStatement)) return false;
        RelationshipTypeReturnStatement that = (RelationshipTypeReturnStatement) o;
        return relationshipVar.equals(that.relationshipVar) && Objects.equals(alias, that.alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(relationshipVar, alias);
    }

    @Override
    public String toString() {
        return "TYPE(" + relationshipVar.getName() + ") " + ( alias != null? " AS "+ alias.getName() : "");
    }
}
