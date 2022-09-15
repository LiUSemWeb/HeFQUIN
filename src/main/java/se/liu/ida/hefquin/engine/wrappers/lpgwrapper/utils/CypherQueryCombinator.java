package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.condition.JoinCondition;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.AliasedExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.BooleanCypherExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.EqualityExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CypherQueryCombinator {
    public static CypherQuery combine(final CypherQuery q1, final CypherQuery q2) {
        if (q1 instanceof CypherMatchQuery && q2 instanceof CypherMatchQuery) {
            return combineMatchMatch((CypherMatchQuery) q1, (CypherMatchQuery) q2);
        } else if (q1 instanceof  CypherUnionQuery && q2 instanceof CypherMatchQuery) {
            return combineUnionMatch((CypherUnionQuery) q1, (CypherMatchQuery) q2);
        } else if (q1 instanceof CypherMatchQuery && q2 instanceof CypherUnionQuery) {
            return combineUnionMatch((CypherUnionQuery) q2, (CypherMatchQuery) q1);
        } else if (q1 instanceof CypherUnionQuery && q2 instanceof CypherUnionQuery) {
            return combineUnionUnion((CypherUnionQuery) q1, (CypherUnionQuery) q2);
        } else {
            return null;
        }
    }

    private static CypherMatchQuery combineMatchMatch(final CypherMatchQuery q1, final CypherMatchQuery q2) {
        final CypherQueryBuilder builder = new CypherQueryBuilder();

        final List<MatchClause> matches1 = q1.getMatches();
        final List<MatchClause> matches2 = q2.getMatches();
        final List<NodeMatchClause> nodes1 = matches1.stream().filter(x -> x instanceof NodeMatchClause)
                .map(x -> (NodeMatchClause) x).collect(Collectors.toList());
        final List<NodeMatchClause> nodes2 = matches2.stream().filter(x -> x instanceof NodeMatchClause)
                .map(x -> (NodeMatchClause) x).collect(Collectors.toList());
        final List<EdgeMatchClause> edges1 = matches1.stream().filter(x -> x instanceof EdgeMatchClause)
                .map(x -> (EdgeMatchClause) x).collect(Collectors.toList());
        final List<EdgeMatchClause> edges2 = matches2.stream().filter(x -> x instanceof EdgeMatchClause)
                .map(x -> (EdgeMatchClause) x).collect(Collectors.toList());
        for (final EdgeMatchClause m : edges1){
            builder.add(m);
        }
        for (final EdgeMatchClause m : edges2) {
            if (! edges1.contains(m)) {
                builder.add(m);
            }
        }
        for (final NodeMatchClause m : nodes1) {
            if (edges1.stream().noneMatch(x->x.isRedundantWith(m)) &&
                edges2.stream().noneMatch(x -> x.isRedundantWith(m))) {
                builder.add(m);
            }
        }
        for (final NodeMatchClause m : nodes2) {
            if ((!nodes1.contains(m)) && edges1.stream().noneMatch(x->x.isRedundantWith(m)) &&
                    edges2.stream().noneMatch(x -> x.isRedundantWith(m))) {
                builder.add(m);
            }
        }
        for (final BooleanCypherExpression c : q1.getConditions())
            builder.add(c);
        for (final BooleanCypherExpression c : q2.getConditions())
            builder.add(c);
        for (final UnwindIterator i : q1.getIterators())
            builder.add(i);
        for (final UnwindIterator i : q2.getIterators())
            builder.add(i);
        for (final AliasedExpression r : q1.getReturnExprs())
            builder.add(r);
        for (final AliasedExpression r : q2.getReturnExprs()) {
            if (q1.getReturnExprs().contains(r)) continue;
            if (q1.getAliases().contains(r.getAlias())) {
                builder.add(new EqualityExpression(r.getExpression(),
                        q1.getReturnExprs().stream().filter(x -> x.getAlias().equals(r.getAlias()))
                                .findFirst().get().getExpression()));
            } else {
                builder.add(r);
            }
        }
        return builder.build();
    }

    private static CypherUnionQuery combineUnionMatch(final CypherUnionQuery q1, final CypherMatchQuery q2) {
        final List<CypherMatchQuery> subqueries = new ArrayList<>();
        for (final CypherMatchQuery q : q1.getSubqueries()) {
            final CypherMatchQuery combination = combineMatchMatch(q, q2);
            if (combination == null){
                return null;
            }
            subqueries.add(combination);
        }
        return new CypherUnionQueryImpl(subqueries);
    }

    private static CypherUnionQuery combineUnionUnion(final CypherUnionQuery q1, final CypherUnionQuery q2) {
        final List<CypherMatchQuery> subqueries = new ArrayList<>();
        for (final CypherMatchQuery q : q1.getSubqueries()) {
            final CypherUnionQuery combination = combineUnionMatch(q2, q);
            if (combination == null) {
                return null;
            }
            subqueries.addAll(combination.getSubqueries());
        }
        return new CypherUnionQueryImpl(subqueries);
    }

}
