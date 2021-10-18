package se.liu.ida.hefquin.engine.utils;

import se.liu.ida.hefquin.engine.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;
import se.liu.ida.hefquin.engine.query.cypher.ReturnStatement;
import se.liu.ida.hefquin.engine.query.cypher.WhereCondition;
import se.liu.ida.hefquin.engine.query.cypher.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.query.impl.CypherMatchQueryImpl;
import se.liu.ida.hefquin.engine.query.impl.CypherUnionQueryImpl;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class CypherQueryUtils {

    public CypherMatchQuery combine(final CypherMatchQuery q1, final CypherMatchQuery q2) {
        Set<MatchClause> matches = q1.getMatches();
        matches.addAll(q2.getMatches());
        final Set<MatchClause> redundant = new HashSet<>();
        for ( final MatchClause m1 : q1.getMatches() ) {
            for ( final MatchClause m2 : q2.getMatches() ) {
                if (m2 instanceof NodeMatchClause && m1.isRedundantWith(m2)) {
                    redundant.add(m2);
                }
            }
        }
        matches.removeAll(redundant);
        Set<CypherVar> matchVars = matches.stream().map(MatchClause::getVars)
                .flatMap(Set::stream).collect(Collectors.toSet());
        Set<WhereCondition> conditions = new HashSet<>();
        for ( final WhereCondition c : q1.getConditions() ){
            if (compatibleVars(c.getVars(), matchVars)) {
                conditions.add(c);
            }
        }
        for ( final WhereCondition c : q2.getConditions() ){
            if (compatibleVars(c.getVars(), matchVars)) {
                conditions.add(c);
            }
        }
        Set<ReturnStatement> returns = new HashSet<>();
        for ( final ReturnStatement r : q1.getReturnExprs() ) {
            if (compatibleVars(r.getVars(), matchVars)) {
                returns.add(r);
            }
        }
        for ( final ReturnStatement r : q2.getReturnExprs() ) {
            if (compatibleVars(r.getVars(), matchVars)) {
                returns.add(r);
            }
        }
        return new CypherMatchQueryImpl(matches, conditions, returns);
    }

    public CypherUnionQuery combine(final CypherUnionQuery q1, final CypherMatchQuery q2) {
        Set<CypherMatchQuery> union = new HashSet<>();
        for (final CypherMatchQuery q : q1.getUnion()) {
            union.add(combine(q, q2));
        }
        return new CypherUnionQueryImpl(union);
    }

    public CypherUnionQuery combine(final CypherUnionQuery q1, final CypherUnionQuery q2) {
        Set<CypherMatchQuery> union = new HashSet<>();
        for (final CypherMatchQuery q3 : q1.getUnion()) {
            for (final CypherMatchQuery q4 : q2.getUnion()){
                union.add(combine(q3, q4));
            }
        }
        return new CypherUnionQueryImpl(union);
    }

    private boolean compatibleVars(Set<CypherVar> inVars, Set<CypherVar> currentVars) {
        return currentVars.containsAll(inVars);
    }
}
