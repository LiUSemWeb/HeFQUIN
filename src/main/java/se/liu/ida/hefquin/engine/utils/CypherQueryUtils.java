package se.liu.ida.hefquin.engine.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.ReturnStatement;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.WhereCondition;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherMatchQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class CypherQueryUtils {

    /**
     * The combine methods merge the sets of match patterns, where conditions and return statements
     * of two cypher queries. This method does not (as of yet) checks for the validity of the resulting
     * queries (for instance, by checking that all the aliases used in the RETURN clause are different).
     * However, the method avoids the introduction of conditions or return statements that use variables
     * not already defined in the MATCH clauses of the query.
     * @param q1 a CypherQuery Object
     * @param q2 a CypherQuery Object
     * @return the combination of q1 and q2.
     */
    public static CypherMatchQuery combine(final CypherMatchQuery q1, final CypherMatchQuery q2) {
        final List<MatchClause> matches = new ArrayList<>();
        for ( final MatchClause m1 : q1.getMatches() ) {
            boolean redundant = false;
            for ( final MatchClause m2 : q2.getMatches() ) {
                if (m1 instanceof NodeMatchClause && m1.isRedundantWith(m2)) {
                    redundant = true;
                    break;
                }
            }
            if (!redundant) {
                matches.add(m1);
            }
        }
        for (final MatchClause m2 : q2.getMatches()) {
            boolean redundant = false;
            for (final MatchClause m : matches) {
                if (m2 instanceof NodeMatchClause && m2.isRedundantWith(m)) {
                    redundant = true;
                    break;
                }
            }
            if (!redundant && !matches.contains(m2)) {
                matches.add(m2);
            }
        }
        final Set<CypherVar> matchVars = matches.stream().map(MatchClause::getVars)
                .flatMap(Set::stream).collect(Collectors.toSet());
        final List<WhereCondition> conditions = new ArrayList<>();
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
        final List<CypherMatchQuery> union = new ArrayList<>();
        for (final CypherMatchQuery q3 : q1.getUnion()) {
            for (final CypherMatchQuery q4 : q2.getUnion()){
                final CypherMatchQuery combination = combine(q3, q4);
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