package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.GraphQLInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.GraphQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.GraphQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.GraphQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLEndpointInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;

public class GraphQLEndpointInitializerImpl implements GraphQLEndpointInitializer {

    @Override
    public GraphQLEndpoint initializeEndpoint(final String url) throws FederationAccessException {
        final GraphQLInterface iface = new GraphQLInterfaceImpl(url);
        final GraphQLEndpoint tmpEndpoint = new GraphQLEndpointImpl(null, null, iface);
        final GraphQLRequestProcessor requestProcessor = new GraphQLRequestProcessorImpl(5000,5000);
        final GraphQLQuery query = getQuery();
        final GraphQLRequest req = new GraphQLRequestImpl(query);
        final JSONResponse response;
        try {
            response = requestProcessor.performRequest(req, tmpEndpoint);
            final JsonObject jsonObject = response.getJsonObject();
            if(!jsonObject.hasKey("data")){
                throw new FederationAccessException("Error at endpoint.", req, tmpEndpoint);
            }
            final JsonObject data = jsonObject.getObj("data");
            return new GraphQLEndpointImpl(parseTypesAndFields(data), parseEntrypoints(data), iface);
        } 
        catch (final FederationAccessException e) {
            throw e;
        }
    }

    /**
     * Initializes the GraphQL introspection query to be used.
     */
    protected GraphQLQuery getQuery(){
        final TreeSet<String> fieldPaths = new TreeSet<>();

        // Name and kind of type (OBJECT, SCALAR etc.)
        fieldPaths.add("__schema/types/name");
        fieldPaths.add("__schema/types/kind");

        // Fields information
        fieldPaths.add("__schema/types/fields/name");
        fieldPaths.add("__schema/types/fields/type/name");
        fieldPaths.add("__schema/types/fields/type/kind");

        /*
            If introspection type/kind is a "LIST" or "NON_NULL" then the use of introspection
            "ofType" is necessary to unwrap the actual type and name. Using ofType three times
            would allow correct unwrapping of potential Non-nullable lists with non-nullable objects/scalars
            (i.e. [typeName!]!)
        */
        fieldPaths.add("__schema/types/fields/type/ofType/name");
        fieldPaths.add("__schema/types/fields/type/ofType/kind");
        fieldPaths.add("__schema/types/fields/type/ofType/ofType/name");
        fieldPaths.add("__schema/types/fields/type/ofType/ofType/kind");
        fieldPaths.add("__schema/types/fields/type/ofType/ofType/ofType/name");
        fieldPaths.add("__schema/types/fields/type/ofType/ofType/ofType/kind");

        // Query type information for creating GraphQLEntrypoints
        fieldPaths.add("__schema/queryType/name");
        fieldPaths.add("__schema/queryType/fields/name");
        fieldPaths.add("__schema/queryType/fields/type/name");
        fieldPaths.add("__schema/queryType/fields/type/kind");
        fieldPaths.add("__schema/queryType/fields/type/ofType/name");
        fieldPaths.add("__schema/queryType/fields/type/ofType/kind");
        fieldPaths.add("__schema/queryType/fields/type/ofType/ofType/name");
        fieldPaths.add("__schema/queryType/fields/type/ofType/ofType/kind");
        fieldPaths.add("__schema/queryType/fields/type/ofType/ofType/ofType/name");
        fieldPaths.add("__schema/queryType/fields/type/ofType/ofType/ofType/kind");
        fieldPaths.add("__schema/queryType/fields/args/name");
        fieldPaths.add("__schema/queryType/fields/args/type/name");
        fieldPaths.add("__schema/queryType/fields/args/type/kind");
        fieldPaths.add("__schema/queryType/fields/args/type/ofType/name");
        fieldPaths.add("__schema/queryType/fields/args/type/ofType/kind");

        // Types to ignore when parsing and initializing types
        fieldPaths.add("__schema/subscriptionType/name");
        fieldPaths.add("__schema/mutationType/name");

        return new GraphQLQueryImpl(fieldPaths, new JsonObject(), new HashMap<>());
    }

    /**
     * Parses introspection data to determine information about a specific GraphQL field.
     * @return a Pair consisting of the GraphQL valuetype (not including list and/or non-nullable identifiers) 
     * and GraphQLFieldType respectively for the GraphQL type. @param field is a JsonObject and should 
     * contain the keys: "name", "kind" and alternatively "ofType"
     */
    protected Pair<String,GraphQLFieldType> determineTypeInformation(final JsonObject field){

        // If current object is a wrapper type (i.e. LIST or NON-NULLABLE) use recursion
        if(field.hasKey("ofType") && !field.get("ofType").isNull()){
            return determineTypeInformation(field.getObj("ofType"));
        }

        final String name = field.getString("name");
        final String kind = field.getString("kind");
        final GraphQLFieldType fieldType;
        if(kind.equals("OBJECT")){
            fieldType = GraphQLFieldType.OBJECT;
        }
        else{
            fieldType = GraphQLFieldType.SCALAR;
        }
        return new Pair<String,GraphQLFieldType>(name,fieldType);
    }


    /**
     * Initializes GraphQLEntrypoints by parsing the __schema/queryType parts of the json.
     */
    protected Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> parseEntrypoints(final JsonObject data) {
        final Map<String, Map<GraphQLEntrypointType, GraphQLEntrypoint>> objectTypeToEntrypoint = new HashMap<>();
        final JsonObject queryType = data.getObj("__schema").getObj("queryType");

        for (final JsonObject field : queryType.getArray("fields").toArray(JsonObject[]::new)) {
            final String fieldName = field.getString("name");

            // Get arguments for field
            final Map<String, String> argumentDefinitions = new HashMap<>();

            for (final JsonObject argument : field.getArray("args").toArray(JsonObject[]::new)) {
                // System.out.println(argument.toString());
                final String argName = argument.getString("name");
                final JsonObject argType = argument.getObj("type");
                if (argType.get("ofType").isNull()) {
                    argumentDefinitions.put(argName, argType.getString("name"));
                } else {
                    final String nonNullable = argType.getString("kind").equals("NON_NULL") ? "!" : "";
                    argumentDefinitions.put(argName, argType.getObj("ofType").getString("name") + nonNullable);
                }
            }

            // Determine GraphQLEntrypointType
            final GraphQLEntrypointType epType;
            if (argumentDefinitions.containsKey("id") && argumentDefinitions.get("id").equals("ID!")) {
                epType = GraphQLEntrypointType.SINGLE;
            } else if (argumentDefinitions.isEmpty()) {
                epType = GraphQLEntrypointType.FULL;
            } else {
                epType = GraphQLEntrypointType.FILTERED;
            }

            // Get type of field
            final Pair<String, GraphQLFieldType> fieldInfo = determineTypeInformation(field.getObj("type"));
            final GraphQLEntrypoint entrypoint = new GraphQLEntrypointImpl(fieldName, argumentDefinitions,
                    fieldInfo.object1);

            if (!objectTypeToEntrypoint.containsKey(fieldInfo.object1)) {
                objectTypeToEntrypoint.put(fieldInfo.object1, new HashMap<>());
            }

            objectTypeToEntrypoint.get(fieldInfo.object1).put(epType, entrypoint);
        }
        return objectTypeToEntrypoint;
    }

    /**
     * Initializes the available GraphQL types and fields received from the response.
     */
    protected Map<String, Map<String, GraphQLField>> parseTypesAndFields(final JsonObject data) {
        final Map<String, Map<String, GraphQLField>> objectTypeToFields = new HashMap<>();

        // Types that shouldn't be initialized (queryType,mutationType,subscriptionType etc)
        final Set<String> typesToSkip = new HashSet<>();
        final JsonValue queryType = data.getObj("__schema").get("queryType");
        final JsonValue subscriptionType = data.getObj("__schema").get("subscriptionType");
        final JsonValue mutationType = data.getObj("__schema").get("mutationType");
        if(!queryType.isNull()){
            typesToSkip.add(queryType.getAsObject().getString("name"));
        }
        if(!subscriptionType.isNull()){
            typesToSkip.add(subscriptionType.getAsObject().getString("name"));
        }
        if(!mutationType.isNull()){
            typesToSkip.add(mutationType.getAsObject().getString("name"));
        }


        // Initialize GraphQL object types and their respective fields
        for (final JsonObject type : data.getObj("__schema").getArray("types").toArray(JsonObject[]::new)) {

            // Filter away JsonObjects that aren't of the GraphQL type OBJECT
            final String typeKind = type.getString("kind");
            if (typeKind == null || !typeKind.equals("OBJECT")) {
                continue;
            }
            // Filter away JsonObjects that are GraphQL standard types
            final String typeName = type.getString("name");
            if (typeName == null || typeName.startsWith("__") || typesToSkip.contains(typeName)) {
                continue;
            }

            final Map<String, GraphQLField> fields = new HashMap<>();

            for (final JsonObject field : type.getArray("fields").toArray(JsonObject[]::new)) {
                final String fieldName = field.getString("name");
                final Pair<String, GraphQLFieldType> fieldInfo = determineTypeInformation(field.getObj("type"));
                fields.put(fieldName, new GraphQLFieldImpl(fieldName, fieldInfo.object1, fieldInfo.object2));
            }

            objectTypeToFields.put(typeName, fields);
        }

        return objectTypeToFields;
    }
}
