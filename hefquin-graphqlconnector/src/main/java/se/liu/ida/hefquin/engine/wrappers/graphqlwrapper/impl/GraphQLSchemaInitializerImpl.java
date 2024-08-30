package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.json.JsonObject;
import org.apache.jena.atlas.json.JsonValue;
import org.apache.jena.atlas.json.io.parserjavacc.javacc.ParseException;

import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLException;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.GraphQLSchemaInitializer;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.conn.GraphQLConnection;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLSchemaImpl;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.impl.GraphQLQueryImpl;

public class GraphQLSchemaInitializerImpl implements GraphQLSchemaInitializer {

    // Introspection constant variables used to build the GraphQL introspection paths
    // and also parse the JSON results with
    static final protected String iSchema = "__schema";
    static final protected String iName = "name";
    static final protected String iKind = "kind";
    static final protected String iType = "type";
    static final protected String iTypes = "types";
    static final protected String iOfType = "ofType";
    static final protected String iFields = "fields";
    static final protected String iQueryType = "queryType";
    static final protected String iQueryArgs = "args";
    static final protected String iSubscriptionType = "subscriptionType";
    static final protected String iMutationType = "mutationType";

    // Base sub-path segment for introspection querying
    static final protected String schemaPath = iSchema + "/";

    // Field paths segments for the GraphQL types
    static final protected String typePath1 = schemaPath + iTypes + "/";
    static final protected String typePath2 = typePath1 + iFields + "/";
    static final protected String typePath3 = typePath2 + iType + "/";

    // Field path segments for the GraphQL "query" type
    static final protected String queryTypePath1 = schemaPath + iQueryType + "/";
    static final protected String queryTypePath2 = queryTypePath1 + iFields + "/";
    static final protected String queryTypePath3 = queryTypePath2 + iType + "/";

    static final protected String queryTypeArgsPath1 = queryTypePath2 + iQueryArgs + "/";
    static final protected String queryTypeArgsPath2 = queryTypeArgsPath1 + iType + "/";

    static final protected String ofTypePath = iOfType + "/";

    // Misc. paths used to filter what graphql types not to include
    static final protected String subscriptionPath = schemaPath + iSubscriptionType + "/";
    static final protected String mutationPath = schemaPath + iMutationType + "/";

    @Override
    public GraphQLSchema initializeSchema(  final String url,
                                            final int connectionTimeout,
                                            final int readTimeout) throws GraphQLException, ParseException {

        final GraphQLQuery introspectionQuery = getIntrospectionQuery();
        try {
            final JsonObject jsonObject = GraphQLConnection.performRequest(introspectionQuery, url, connectionTimeout, readTimeout );
            if(!jsonObject.hasKey("data")){
                throw new GraphQLException("Error at endpoint.");
            }
            final JsonObject data = jsonObject.getObj("data");

            return validateTypesAndEntrypoints(parseTypesAndFields(data),parseEntrypoints(data));
        } 
        catch (final GraphQLException e) {
            throw e;
        }
    }

    /**
     * @return the GraphQL introspection query to be used.
     */
    protected static GraphQLQuery getIntrospectionQuery(){
        final Set<String> fieldPaths = new HashSet<>();

        // Name and kind of type (OBJECT, SCALAR etc.)
        fieldPaths.add(typePath1 + iName);
        fieldPaths.add(typePath1 + iKind);

        // Fields information
        fieldPaths.add(typePath2 + iName);
        fieldPaths.add(typePath3 + iName);
        fieldPaths.add(typePath3 + iKind);

        /*
            If introspection type/kind is a "LIST" or "NON_NULL" then the use of introspection
            "ofType" is necessary to unwrap the actual type and name. Using ofType three times
            would allow correct unwrapping of potential Non-nullable lists with non-nullable objects/scalars
            (i.e. [typeName!]!)
        */
        fieldPaths.add(typePath3 + ofTypePath + iName);
        fieldPaths.add(typePath3 + ofTypePath + iKind);
        fieldPaths.add(typePath3 + ofTypePath + ofTypePath + iName);
        fieldPaths.add(typePath3 + ofTypePath + ofTypePath + iKind);
        fieldPaths.add(typePath3 + ofTypePath + ofTypePath + ofTypePath + iName);
        fieldPaths.add(typePath3 + ofTypePath + ofTypePath + ofTypePath + iKind);

        // ------------------------------------------------------
        // Query type information for creating GraphQLEntrypoints
        fieldPaths.add(queryTypePath1 + iName);
        fieldPaths.add(queryTypePath2 + iName);
        fieldPaths.add(queryTypePath3 + iName);
        fieldPaths.add(queryTypePath3 + iKind);

        // "Unwrap" query type
        fieldPaths.add(queryTypePath3 + ofTypePath + iName);
        fieldPaths.add(queryTypePath3 + ofTypePath + iKind);
        fieldPaths.add(queryTypePath3 + ofTypePath + ofTypePath + iName);
        fieldPaths.add(queryTypePath3 + ofTypePath + ofTypePath + iKind);
        fieldPaths.add(queryTypePath3 + ofTypePath + ofTypePath + ofTypePath + iName);
        fieldPaths.add(queryTypePath3 + ofTypePath + ofTypePath + ofTypePath + iKind);

        // ------------------------------------------------------
        // query type args
        fieldPaths.add(queryTypeArgsPath1 + iName);
        fieldPaths.add(queryTypeArgsPath2 + iName);
        fieldPaths.add(queryTypeArgsPath2 + iKind);
        fieldPaths.add(queryTypeArgsPath2 + ofTypePath + iName);
        fieldPaths.add(queryTypeArgsPath2 + ofTypePath + iKind);

        // ------------------------------------------------------
        // Types to ignore when parsing and initializing types
        fieldPaths.add(subscriptionPath + iName);
        fieldPaths.add(mutationPath + iName);

        return new GraphQLQueryImpl(fieldPaths, new HashSet<>());
    }

    /**
     * Removes the types, fields and entrypoints from the @param typesAndFields and @param entrypoints
     * and notes their removal if they don't meet the criteria for the approach. (each type needs an id field etc.)
     */
    protected GraphQLSchema validateTypesAndEntrypoints(final Map<String, Map<String, GraphQLField>> typesAndFields,
            final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> entrypoints){

        final Set<String> typesToRemove = new HashSet<>();

        // Check if any types needs to be removed
        for(final String type : typesAndFields.keySet()){
            final Map<String,GraphQLField> fields = typesAndFields.get(type);

            // Check if type has an id field
            if(!fields.containsKey("id")){
                typesToRemove.add(type);
                continue;
            }

            // Check if type has atleast one valid entrypoint
            if(!entrypoints.containsKey(type) || entrypoints.get(type).isEmpty()){
                typesToRemove.add(type);
            }
        }

        // Remove fields that links to types that will be removed
        for(final String type : typesAndFields.keySet()) {

            // If entire type is to be removed, skip removing individual fields
            if(typesToRemove.contains(type)){
                continue;
            }

            final Map<String,GraphQLField> fields = typesAndFields.get(type);
            final Set<String> fieldsToRemove = new HashSet<>();

            for(final String field : fields.keySet()){
                final GraphQLField fieldInfo = fields.get(field);

                // Check if value type of current field is a type that will be removed
                if(fieldInfo.getFieldType() == GraphQLFieldType.OBJECT && typesToRemove.contains(fieldInfo.getValueType())){
                    fieldsToRemove.add(field);
                }
            }

            // Remove fields from type
            fields.keySet().removeAll(fieldsToRemove);
        }

        // Remove specific types and their entrypoints
        typesAndFields.keySet().removeAll(typesToRemove);
        entrypoints.keySet().removeAll(typesToRemove);

        return new GraphQLSchemaImpl(typesAndFields,entrypoints);
    }

    /**
     * Parses introspection data to determine information about a specific GraphQL
     * field.
     * 
     * @return a Pair consisting of the GraphQL valuetype (not including list and/or
     *         non-nullable identifiers) and GraphQLFieldType respectively for the
     *         GraphQL type. @param field is a JsonObject and should contain the
     *         keys: "name", "kind" and alternatively "ofType"
     * @throws ParseException
     */
    protected Pair<String, GraphQLFieldType> determineTypeInformation(final JsonObject field) throws ParseException {

        // ofTypePath segment without '/' at the end
        final String ofTypeKey = ofTypePath.substring(0,ofTypePath.length() - 1);

        // If current object is a wrapper type (i.e. LIST or NON-NULLABLE) use recursion
        if(field.hasKey(ofTypeKey) && !field.get(ofTypeKey).isNull()){
            return determineTypeInformation(field.getObj(ofTypeKey));
        }

        final String name = field.getString(iName);
        final String kind = field.getString(iKind);
        final GraphQLFieldType fieldType;

        if(kind.equals("OBJECT") || kind.equals("INTERFACE")){
            fieldType = GraphQLFieldType.OBJECT;
        }
        else if(kind.equals("SCALAR") || kind.equals("ENUM")){
            fieldType = GraphQLFieldType.SCALAR;
        }
        else{
            throw new ParseException("The \"kind\" value of the provided json object is invalid");
        }
        return new Pair<String,GraphQLFieldType>(name,fieldType);
    }


    /**
     * Initializes GraphQLEntrypoints by parsing the __schema/queryType parts of the json.
     */
    protected Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> parseEntrypoints(final JsonObject data) 
            throws ParseException{

        final Map<String, Map<GraphQLEntrypointType, GraphQLEntrypoint>> objectTypeToEntrypoint = new HashMap<>();
        final JsonObject queryType = data.getObj(iSchema).getObj(iQueryType);

        for (final JsonObject field : queryType.getArray(iFields).toArray(JsonObject[]::new)) {
            final String fieldName = field.getString(iName);

            // Get arguments for field
            final Map<String, String> argumentDefinitions = new HashMap<>();

            for (final JsonObject argument : field.getArray(iQueryArgs).toArray(JsonObject[]::new)) {
                final String argName = argument.getString(iName);
                final JsonObject argType = argument.getObj(iType);
                if (argType.get(iOfType).isNull()) {
                    argumentDefinitions.put(argName, argType.getString(iName));
                } else {
                    final String nonNullable = argType.getString(iKind).equals("NON_NULL") ? "!" : "";
                    argumentDefinitions.put(argName, argType.getObj(iOfType).getString(iName) + nonNullable);
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
            final Pair<String, GraphQLFieldType> fieldInfo = determineTypeInformation(field.getObj(iType));
            final GraphQLEntrypoint entrypoint = new GraphQLEntrypointImpl(fieldName, argumentDefinitions,
                    fieldInfo.object1, epType);

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
    protected Map<String, Map<String, GraphQLField>> parseTypesAndFields(final JsonObject data) 
            throws ParseException {

        final Map<String, Map<String, GraphQLField>> objectTypeToFields = new HashMap<>();

        // Types that shouldn't be initialized (queryType,mutationType,subscriptionType etc)
        final Set<String> typesToSkip = new HashSet<>();
        final JsonValue queryType = data.getObj(iSchema).get(iQueryType);
        final JsonValue subscriptionType = data.getObj(iSchema).get(iSubscriptionType);
        final JsonValue mutationType = data.getObj(iSchema).get(iMutationType);
        if(!queryType.isNull()){
            typesToSkip.add(queryType.getAsObject().getString(iName));
        }
        if(!subscriptionType.isNull()){
            typesToSkip.add(subscriptionType.getAsObject().getString(iName));
        }
        if(!mutationType.isNull()){
            typesToSkip.add(mutationType.getAsObject().getString(iName));
        }


        // Initialize GraphQL object types and their respective fields
        for (final JsonObject type : data.getObj(iSchema).getArray(iTypes).toArray(JsonObject[]::new)) {

            // Filter away JsonObjects that aren't of the GraphQL type OBJECT or INTERFACE
            final String typeKind = type.getString(iKind);
            if (typeKind == null || !(typeKind.equals("OBJECT") || typeKind.equals("INTERFACE"))) {
                continue;
            }
            // Filter away JsonObjects that are GraphQL standard types
            final String typeName = type.getString(iName);
            if (typeName == null || typeName.startsWith("__") || typesToSkip.contains(typeName)) {
                continue;
            }

            final Map<String, GraphQLField> fields = new HashMap<>();

            for (final JsonObject field : type.getArray(iFields).toArray(JsonObject[]::new)) {
                final String fieldName = field.getString(iName);
                final Pair<String, GraphQLFieldType> fieldInfo = determineTypeInformation(field.getObj(iType));
                fields.put(fieldName, new GraphQLFieldImpl(fieldName, fieldInfo.object1, fieldInfo.object2));
            }

            objectTypeToFields.put(typeName, fields);
        }

        return objectTypeToFields;
    }
}
