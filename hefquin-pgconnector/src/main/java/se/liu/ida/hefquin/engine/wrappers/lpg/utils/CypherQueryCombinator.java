package se.liu.ida.hefquin.engine.wrappers.lpg.utils;

import java.util.*;

import se.liu.ida.hefquin.engine.wrappers.lpg.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.expression.*;

public class CypherQueryCombinator {
    public static CypherQuery combine(final CypherQuery q1, final CypherQuery q2, final CypherVarGenerator gen) {
        if (q1 instanceof CypherMatchQuery && q2 instanceof CypherMatchQuery) {
            return combineMatchMatch((CypherMatchQuery) q1, (CypherMatchQuery) q2, gen, -1);
        } else if (q1 instanceof  CypherUnionQuery && q2 instanceof CypherMatchQuery) {
            return combineUnionMatch((CypherUnionQuery) q1, (CypherMatchQuery) q2, gen, 0);
        } else if (q1 instanceof CypherMatchQuery && q2 instanceof CypherUnionQuery) {
            return combineUnionMatch((CypherUnionQuery) q2, (CypherMatchQuery) q1, gen, 0);
        } else if (q1 instanceof CypherUnionQuery && q2 instanceof CypherUnionQuery) {
            return combineUnionUnion((CypherUnionQuery) q1, (CypherUnionQuery) q2, gen);
        } else {
            return null;
        }
    }

    private static CypherMatchQuery combineMatchMatch(final CypherMatchQuery q1, final CypherMatchQuery q2,
                                                      final CypherVarGenerator gen, final int index) {
        if ( hasInvalidJoins(q1, q2) ) {
            return null;
        }
        final CypherQueryBuilder builder = new CypherQueryBuilder();
        for (final MatchClause m : q1.getMatches()){
            builder.add(m);
        }
        for (final MatchClause m : q2.getMatches()) {
            if (! q1.getMatches().contains(m)) {
                builder.add(m);
            }
        }
        for (final BooleanCypherExpression c : q1.getConditions())
            builder.add(c);
        for (final BooleanCypherExpression c : q2.getConditions())
            builder.add(c);
        final Set<CypherVar> uvars1 = q1.getUvars();
        final Set<CypherVar> uvars2 = q2.getUvars();
        final Map<CypherVar, List<UnwindIterator>> iteratorJoin = new HashMap<>();
        for (final UnwindIterator i : q1.getIterators()) {
            if (q1.getReturnExprs().stream().noneMatch(x -> q2.getAliases().contains(x.getAlias())
                    && x.getVars().contains(i.getAlias()))) {
                builder.add(i);
            } else {
                final List<UnwindIterator> list = new ArrayList<>();
                list.add(i);
                iteratorJoin.put(q1.getReturnExprs().stream().filter(x -> x.getVars().contains(i.getAlias()))
                        .findFirst().get().getAlias(), list);
            }
        }
        for (final UnwindIterator i : q2.getIterators()) {
            if (q2.getReturnExprs().stream().noneMatch(x -> q1.getAliases().contains(x.getAlias())
                    && x.getVars().contains(i.getAlias()))) {
                builder.add(i);
            } else {
                iteratorJoin.get(q2.getReturnExprs().stream().filter(x -> x.getVars().contains(i.getAlias()))
                        .findFirst().get().getAlias()).add(i);
            }
        }
        final Map<CypherVar, CypherVar> unwindVarsMap = new HashMap<>();
        for (final Map.Entry<CypherVar,List<UnwindIterator>> e : iteratorJoin.entrySet()) {
            final UnwindIterator u1 = e.getValue().get(0);
            final UnwindIterator u2 = e.getValue().get(1);
            final CypherVar anonVar = gen.getAnonVar();
            unwindVarsMap.put(u1.getAlias(), anonVar);
            unwindVarsMap.put(u2.getAlias(), anonVar);
            builder.add(combineLists(u1, u2, anonVar));
        }
        if (index > -1) {
            builder.add(new MarkerExpression(index, gen.getMarkerVar()));
        }
        List<AliasedExpression> combinedAliasedExpressions = new ArrayList<>();
        for (final AliasedExpression r : q1.getReturnExprs()) {
            if (r instanceof MarkerExpression) continue;
            if (r.getExpression() instanceof GetItemExpression) {
                final GetItemExpression ex = (GetItemExpression) r.getExpression();
                final CypherVar unwindVar = (CypherVar) ex.getExpression();
                if (unwindVarsMap.containsKey(unwindVar)) {
                    combinedAliasedExpressions.add(new AliasedExpression(new GetItemExpression(
                            unwindVarsMap.get(unwindVar), ex.getIndex()), r.getAlias()));
                } else combinedAliasedExpressions.add(r);
            } else {
                combinedAliasedExpressions.add(r);
            }
        }
        for (final AliasedExpression r : q2.getReturnExprs()) {
            if (r instanceof MarkerExpression) continue;
            final AliasedExpression mappedR;
            if (r.getExpression() instanceof GetItemExpression) {
                final GetItemExpression ex = (GetItemExpression) r.getExpression();
                final CypherVar unwindVar = (CypherVar) ex.getExpression();
                if (unwindVarsMap.containsKey(unwindVar)) {
                    mappedR = new AliasedExpression(new GetItemExpression(unwindVarsMap.get(unwindVar),
                            ex.getIndex()), r.getAlias());
                } else {
                    mappedR = r;
                }
            } else {
                mappedR = r;
            }
            if (combinedAliasedExpressions.contains(mappedR)) {
                continue;
            }
            if (q1.getAliases().contains(mappedR.getAlias())) {
                final AliasedExpression r1 = q1.getReturnExprs().stream()
                        .filter(x -> x.getAlias().equals(r.getAlias())).findFirst().get();
                if (!uvars2.containsAll(r.getVars()) && !uvars1.containsAll(r1.getVars())) {
                    builder.add(new EqualityExpression(r.getExpression(), r1.getExpression()));
                }
            } else {
                combinedAliasedExpressions.add(mappedR);
            }
        }
        for (final AliasedExpression ex : combinedAliasedExpressions) {
            builder.add(ex);
        }
        return builder.build();
    }

    private static UnwindIterator combineLists(final UnwindIterator u1, final UnwindIterator u2,
                                               final CypherVar anonVar) {
        final List<BooleanCypherExpression> filters = new ArrayList<>(u1.getFilters());
        filters.addAll(u2.getFilters());
        filters.add(new MembershipExpression(u1.getInnerVar(), u2.getListExpression()));
        final List<CypherExpression> retExprs = new ArrayList<>(u1.getReturnExpressions());
        for (final CypherExpression ex : u2.getReturnExpressions()) {
            if (! retExprs.contains(ex))
                retExprs.add(ex);
        }
        return new UnwindIteratorImpl(u1.getInnerVar(), u1.getListExpression(), filters, retExprs, anonVar);
    }

    private static boolean hasInvalidJoins(final CypherMatchQuery q1, final CypherMatchQuery q2) {
        final List<AliasedExpression> ret1 = q1.getReturnExprs();
        final List<AliasedExpression> ret2 = q2.getReturnExprs();
        final List<CypherVar> aliases2 = q2.getAliases();
        final Set<CypherVar> uvars1 = q1.getUvars();
        final Set<CypherVar> uvars2 = q2.getUvars();

        for (final AliasedExpression e : ret1) {
            if (aliases2.contains(e.getAlias())) {
                final CypherExpression exp = ret2.stream().filter(x -> x.getAlias().equals(e.getAlias())).findFirst().get();
                if (!Collections.disjoint(exp.getVars(), uvars2)^!Collections.disjoint(e.getVars(), uvars1))
                    return true;
            }
        }
        return false;
    }

    private static CypherUnionQuery combineUnionMatch(final CypherUnionQuery q1, final CypherMatchQuery q2,
                                                      final CypherVarGenerator gen, final int offset) {
        final List<CypherMatchQuery> subqueries = new ArrayList<>();
        int index = offset;
        for (final CypherMatchQuery q : q1.getSubqueries()) {
            final CypherMatchQuery combination = combineMatchMatch(q, q2, gen, index);
            if (combination == null){
                continue;
            }
            index++;
            subqueries.add(combination);
        }
        if (subqueries.isEmpty()) return null;
        return new CypherUnionQueryImpl(subqueries);
    }

    private static CypherUnionQuery combineUnionUnion(final CypherUnionQuery q1, final CypherUnionQuery q2,
                                                      final CypherVarGenerator gen) {
        final List<CypherMatchQuery> subqueries = new ArrayList<>();
        int offset = 0;
        for (final CypherMatchQuery q : q1.getSubqueries()) {
            final CypherUnionQuery combination = combineUnionMatch(q2, q, gen, offset);
            if (combination == null) {
                continue;
            }
            offset += combination.getSubqueries().size();
            subqueries.addAll(combination.getSubqueries());
        }
        if (subqueries.isEmpty()) return null;
        return new CypherUnionQueryImpl(subqueries);
    }

}
