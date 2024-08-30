package se.liu.ida.hefquin.engine.wrappers.graphql.query.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;

public class GraphQLQueryImpl implements GraphQLQuery {
    protected final Set<String> fieldPaths;
    protected final Set<GraphQLArgument> queryArgs;

    public GraphQLQueryImpl(final Set<String> fieldPaths, final Set<GraphQLArgument> queryArgs) {
        this.fieldPaths = fieldPaths;
        this.queryArgs = queryArgs;
    }

    public String toString() {
        final StringBuilder query = new StringBuilder();
        if (!queryArgs.isEmpty()) {
            query.append("query(");
            final Map<String,String> argDefs = getArgumentDefinitions();
            for (final String argName : argDefs.keySet()) {
                query.append("$").append(argName).append(":");
                query.append(argDefs.get(argName));
                query.append(",");
            }
            query.append(")");
        }
        query.append(buildQueryString());

        return query.toString();
    }

    @Override
    public Set<String> getFieldPaths() {
        return fieldPaths;
    }

    @Override
    public JsonObject getArgumentValues() {
        final JsonObject obj = new JsonObject();
        for(final GraphQLArgument e : queryArgs){
            obj.put(e.getVariableName(),e.getArgValue());
        }
        return obj;
    }

    @Override
    public Map<String, String> getArgumentDefinitions() {
        Map<String,String> argDefs = new HashMap<>();
        for(final GraphQLArgument e : queryArgs){
            argDefs.put(e.getVariableName(), e.getArgDefinition());
        }
        return argDefs;
    }

    protected String buildQueryString() {
        final StringBuilder query = new StringBuilder();
        query.append("{");
        StringBuilder path = new StringBuilder();
        int depth = 0;

        for (final String currentPath : new TreeSet<>(fieldPaths)) {

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
