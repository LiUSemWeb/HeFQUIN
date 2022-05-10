package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl;

import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

public class GraphQLQueryImpl implements GraphQLQuery {
    protected final TreeSet<String> fieldPaths;
    protected final JsonObject parameterValues;
    protected final TreeMap<String, String> parameterDefinitions;

    public GraphQLQueryImpl(final TreeSet<String> fieldPaths, final JsonObject parameterValues,
            final TreeMap<String, String> parameterDefinitions) {
        this.fieldPaths = fieldPaths;
        this.parameterValues = parameterValues;
        this.parameterDefinitions = parameterDefinitions;
    }

    public String toString() {
        String query = "";
        if (!parameterDefinitions.isEmpty()) {
            query += "query(";
            for (String parameterName : parameterDefinitions.keySet()) {
                query += "$" + parameterName + ":" + parameterDefinitions.get(parameterName) + ",";
            }
            query += ")";
        }
        query += buildQueryString();

        return query;
    }

    @Override
    public final JsonObject getParameterValues() {
        return parameterValues;
    }

    protected String buildQueryString() {
        String query = "";
        String path = "";
        int depth = 0;

        for (String currentPath : fieldPaths) {

            int splitIndex = currentPath.lastIndexOf("/") + 1;
            String domain = currentPath.substring(0, splitIndex);
            String field = currentPath.substring(splitIndex);

            // Parse out if domain of currentPath starts differently than actual path
            while (!domain.startsWith(path)) {
                query += "},";
                int i = path.lastIndexOf("/", path.length() - 2);
                path = (i > 0) ? path.substring(0, i + 1) : "";
                --depth;
            }

            // Parse in if domain and path are different, keep adding parts from domain to
            // path until they are the same
            int begin = path.length();
            while (!path.equals(domain)) {
                int i = domain.indexOf("/", begin);
                String domainPart = (i > 0) ? domain.substring(begin, i) : domain.substring(begin);
                query += domainPart + "{";
                path += domainPart + "/";
                begin = i + 1;
                ++depth;
            }

            query += field + ",";
        }

        // Parse out completely after final field is added
        while (depth > 0) {
            query += "}";
            --depth;
        }

        return "{" + query + "}";
    }
}
