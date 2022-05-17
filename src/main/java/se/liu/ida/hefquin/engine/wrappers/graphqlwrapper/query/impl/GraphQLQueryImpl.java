package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl;

import java.util.Map;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

public class GraphQLQueryImpl implements GraphQLQuery {
    protected final TreeSet<String> fieldPaths;
    protected final JsonObject parameterValues;
    protected final Map<String, String> parameterDefinitions;

    public GraphQLQueryImpl(final TreeSet<String> fieldPaths, final JsonObject parameterValues,
            final Map<String, String> parameterDefinitions) {
        this.fieldPaths = fieldPaths;
        this.parameterValues = parameterValues;
        this.parameterDefinitions = parameterDefinitions;
    }

    public String toString() {
        final StringBuilder query = new StringBuilder();
        if (!parameterDefinitions.isEmpty()) {
            query.append("query(");
            for (final String parameterName : parameterDefinitions.keySet()) {
                query.append("$").append(parameterName).append(":");
                query.append(parameterDefinitions.get(parameterName));
                query.append(",");
            }
            query.append(")");
        }
        query.append(buildQueryString());

        return query.toString();
    }

    @Override
    public JsonObject getParameterValues() {
        return parameterValues;
    }

    protected String buildQueryString() {
        final StringBuilder query = new StringBuilder();
        query.append("{");
        StringBuilder path = new StringBuilder();
        int depth = 0;

        for (final String currentPath : fieldPaths) {

            final int splitIndex = currentPath.lastIndexOf("/") + 1;
            final String domain = currentPath.substring(0, splitIndex);
            final String field = currentPath.substring(splitIndex);

            // Parse out if domain of currentPath starts differently than actual path
            while (!domain.startsWith(path.toString())) {
                query.append( "},");
                final int i = path.lastIndexOf("/", path.length() - 2);
                path = (i > 0) ? new StringBuilder(path.substring(0, i + 1)) : new StringBuilder();
                --depth;
            }

            // Parse in if domain and path are different, keep adding parts from domain to
            // path until they are the same
            int begin = path.length();
            while (!path.toString().equals(domain)) {
                final int i = domain.indexOf("/", begin);
                final String domainPart = (i > 0) ? domain.substring(begin, i) : domain.substring(begin);
                query.append(domainPart).append("{");
                path.append(domainPart).append("/");
                begin = i + 1;
                ++depth;
            }

            query.append(field).append(",");
        }

        // Parse out completely after final field is added
        while (depth > 0) {
            query.append("}");
            --depth;
        }

        return query.append("}").toString();
    }
}
