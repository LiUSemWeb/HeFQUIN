package se.liu.ida.hefquin.engine.query.utils;

import se.liu.ida.hefquin.engine.query.CypherQuery;
import se.liu.ida.hefquin.engine.query.cypher.CypherVar;
import se.liu.ida.hefquin.engine.query.cypher.EdgeMatchClause;
import se.liu.ida.hefquin.engine.query.cypher.MatchClause;

import java.util.regex.Pattern;

public class MatchVariableGetter {

    static protected Pattern pattern = Pattern.compile("\\[(.*?)\\]");

    static public CypherVar getEdgeVariable(final CypherQuery q) {
        if (q.isMatchQuery()){
            for(final MatchClause m : q.getMatches()) {
                if (m instanceof EdgeMatchClause) {
                    return ((EdgeMatchClause) m).getEdge();
                }
            }
        }
        return null;
    }
}
