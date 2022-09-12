package se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherUnionQuery;

public class CypherQueryCombinator {
    public static CypherQuery combine(CypherQuery q1, CypherQuery q2) {
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

    private static CypherQuery combineMatchMatch(CypherMatchQuery q1, CypherMatchQuery q2) {
        return null;
    }

    private static CypherQuery combineUnionMatch(CypherUnionQuery q1, CypherMatchQuery q2) {
        return null;
    }

    private static CypherQuery combineUnionUnion(CypherUnionQuery q1, CypherUnionQuery q2) {
        return null;
    }

}
