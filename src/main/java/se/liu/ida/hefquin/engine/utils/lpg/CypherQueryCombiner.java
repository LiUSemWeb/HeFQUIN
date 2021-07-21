package se.liu.ida.hefquin.engine.utils.lpg;

import se.liu.ida.hefquin.engine.query.CypherQuery;

public class CypherQueryCombiner {

    public static void combine(final CypherQuery result, final CypherQuery translation) {
        for (final String match : translation.getMatches()) {
            result.addMatchClause(match);
        }
        for (final String cond : translation.getConditions()) {
            result.addConditionConjunction(cond);
        }
        for (final String ret : translation.getReturnExprs()){
            result.addReturnClause(ret);
        }
    }

}
