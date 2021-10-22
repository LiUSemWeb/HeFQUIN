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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CypherQueryUtils {

    /**
     * The combine methods merge the sets of match patterns, where conditions and return statements
     * of two cypher queries. This method does not (as of yet) checks for the validity of the resulting queries.
     * @param q1 a CypherQuery Object
     * @param q2 a CypherQuery Object
     * @return the combination of q1 and q2.
     */
    public static CypherMatchQuery combine(final CypherMatchQuery q1, final CypherMatchQuery q2) {
        final List<MatchClause> matches = q1.getMatches();
        final Set<MatchClause> redundant = new HashSet<>();
        for ( final MatchClause m1 : q1.getMatches() ) {
            for ( final MatchClause m2 : q2.getMatches() ) {
                if (!matches.contains(m2)){
                    matches.add(m2);
                }
                if (m2 instanceof NodeMatchClause && m1.isRedundantWith(m2)) {
                    redundant.add(m2);
                }
            }
        }
        matches.removeAll(redundant);
        Set<CypherVar> matchVars = matches.stream().map(MatchClause::getVars)
                .flatMap(Set::stream).collect(Collectors.toSet());
        List<WhereCondition> conditions = new ArrayList<>();
        for ( final WhereCondition c : q1.getConditions() ){
            if (!conditions.contains(c) && compatibleVars(c.getVars(), matchVars)) {
                conditions.add(c);
            }
        }
        for ( final WhereCondition c : q2.getConditions() ){
            if (!conditions.contains(c) && compatibleVars(c.getVars(), matchVars)) {
                conditions.add(c);
            }
        }
        final List<ReturnStatement> returns = new ArrayList<>();
        for ( final ReturnStatement r : q1.getReturnExprs() ) {
            if (!returns.contains(r) && compatibleVars(r.getVars(), matchVars)) {
                returns.add(r);
            }
        }
        for ( final ReturnStatement r : q2.getReturnExprs() ) {
            if (!returns.contains(r) && compatibleVars(r.getVars(), matchVars)) {
                returns.add(r);
            }
        }
        return new CypherMatchQueryImpl(matches, conditions, returns);
    }

    public static CypherUnionQuery combine(final CypherUnionQuery q1, final CypherMatchQuery q2) {
        final List<CypherMatchQuery> union = new ArrayList<>();
        for (final CypherMatchQuery q : q1.getUnion()) {
            final CypherMatchQuery combination = combine(q, q2);
            if (!union.contains(combination)) {
                union.add(combination);
            }
        }
        return new CypherUnionQueryImpl(union);
    }

    public static CypherUnionQuery combine(final CypherMatchQuery q1, final CypherUnionQuery q2) {
        return combine(q2, q1);
    }

    public static CypherUnionQuery combine(final CypherUnionQuery q1, final CypherUnionQuery q2) {
        List<CypherMatchQuery> union = new ArrayList<>();
        for (final CypherMatchQuery q3 : q1.getUnion()) {
            for (final CypherMatchQuery q4 : q2.getUnion()){
                CypherMatchQuery combination = combine(q3, q4);
                if (!union.contains(combination)) {
                    union.add(combination);
                }
            }
        }
        return new CypherUnionQueryImpl(union);
    }

    private static boolean compatibleVars(Set<CypherVar> inVars, Set<CypherVar> currentVars) {
        return currentVars.containsAll(inVars);
    }
}
