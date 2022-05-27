package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.jena.atlas.json.JsonNull;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.graph.impl.LiteralLabel;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLArgumentImpl;

public class GraphQLFieldPathBuilder {

    /**
     * Creates a new String by adding an id field to @param currentPath
     */
    public static String addID(final String currentPath, final String type){
        return currentPath + "id_" + type + ":id";
    }

    /**
     * Creates a new String by adding an object field to @param currentPath
     */
    public static String addObject(final String currentPath, final String alias, final String fieldName){
        return currentPath + "object_" + alias + ":" + fieldName + "/";
    }

    /**
     * Creates a new String by adding a scalar field to @param currentPath
     */
    public static String addScalar(final String currentPath, final String alias, final String fieldName){
        return currentPath + "scalar_" + alias + ":" + fieldName;
    }

    /**
     * Creates the entrypoint portion part of a fieldpath.
     */
    public static String addEntrypoint(final GraphQLEntrypoint e, final Set<GraphQLArgument> queryArgs,
            final Map<String, LiteralLabel> sgpArgs, final MutableInt varCounter, int entrypointCounter){

        // Alias the entrypoint
        final StringBuilder path = new StringBuilder();
        final String entrypointAlias = e.getEntrypointAlias(entrypointCounter);
        path.append(entrypointAlias);

        final Map<String, String> entrypointArgDefs = e.getArgumentDefinitions();
        if (entrypointArgDefs != null && !entrypointArgDefs.isEmpty()) {
            path.append("(");
            for (final String argName : new TreeSet<String>(entrypointArgDefs.keySet())) {
                final String variableName = "var" + varCounter;
                
                final String currArgDefinition = entrypointArgDefs.get(argName);
                final JsonValue currArgValue;

                if (sgpArgs.containsKey(argName)) {
                    currArgValue = SPARQL2GraphQLHelper.literalToJsonValue(sgpArgs.get(argName));
                } else {
                    currArgValue = JsonNull.instance;
                }

                final GraphQLArgument currArg = new GraphQLArgumentImpl(variableName, argName, currArgValue, currArgDefinition);
                queryArgs.add(currArg);
                path.append(currArg).append(",");
                varCounter.increment();
            }
            path.deleteCharAt(path.length()-1);
            path.append(")");
        }

        path.append("/");

        return path.toString();
    }

    /**
     * Creates the entrypoint portion part of a fieldpath. (No entrypoint arguments).
     */
    public static String addNoArgEntrypoint(final GraphQLEntrypoint e, int entrypointCounter){
        return e.getEntrypointAlias(entrypointCounter) + "/";
    }
}
