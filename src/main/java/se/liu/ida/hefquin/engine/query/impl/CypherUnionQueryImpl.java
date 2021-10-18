package se.liu.ida.hefquin.engine.query.impl;

import se.liu.ida.hefquin.engine.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;

import java.util.*;
import java.util.stream.Collectors;

public class CypherUnionQueryImpl implements CypherUnionQuery {

    final Set<CypherMatchQuery> union;

    public CypherUnionQueryImpl() {
        union = new HashSet<>();
    }

    public CypherUnionQueryImpl(CypherMatchQuery... queries) {
        this();
        union.addAll(Arrays.asList(queries));
    }

    public CypherUnionQueryImpl(Set<CypherMatchQuery> result) {
        this.union = result;
    }

    @Override
    public Set<CypherMatchQuery> getUnion() {
        return union;
    }

    private void removeMalformed(final Set<CypherMatchQuery> queries) {
        final List<CypherMatchQuery> toRemove = new LinkedList<>();
        for (final CypherMatchQuery q : queries) {
            final List<CypherVar> seenAliases = new ArrayList<>();
            for (final ReturnStatement r : q.getReturnExprs()) {
                if (seenAliases.contains(r.getAlias())){
                    toRemove.add(q);
                    break;
                } else {
                    seenAliases.add(r.getAlias());
                }
            }
        }
        toRemove.forEach(queries::remove);
    }

    @Override
    public Set<CypherVar> getMatchVars() {
        return union.stream().map(CypherQuery::getMatchVars).flatMap(Set::stream).collect(Collectors.toSet());
    }

    @Override
    public String toString() {
        return union.stream().map(CypherQuery::toString).collect(Collectors.joining("\nUNION\n"));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CypherUnionQueryImpl)) return false;
        CypherUnionQueryImpl that = (CypherUnionQueryImpl) o;
        return union.equals(that.union);
    }

    @Override
    public int hashCode() {
        return Objects.hash(union);
    }
}
