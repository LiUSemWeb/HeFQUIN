package se.liu.ida.hefquin.engine.query.utils;

import se.liu.ida.hefquin.engine.query.CypherQuery;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchVariableGetter {

    static protected Pattern pattern = Pattern.compile("\\[(.*?)\\]");

    static public String getEdgeVariable(final CypherQuery q) {
        final String matchEdge = q.getMatches().stream().filter(s->s.contains("[")).findFirst().get();
        final Matcher matcher = pattern.matcher(matchEdge);
        if (matcher.find()){
            final String res = matcher.group(1);
            if (res.contains(":")){
                return res.split(":")[0];
            }
            return res;
        }
        return "";
    }
}
