package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class GraphQLSchemaImpl implements GraphQLSchema {

    // Maps object type names to a map, which in turn maps field names to GraphQLField objects
    protected final Map<String, Map<String,GraphQLField>> objectTypeToFields;

    // Maps object type names to a map, which in turn maps a GraphQLEntrypointType to a GraphQLEntrypoint
    protected final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> objectTypeToEntrypoint;

    public GraphQLSchemaImpl(final Map<String, Map<String,GraphQLField>> objectTypeToFields,
            final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> objectTypeToEntrypoint){
        this.objectTypeToFields = objectTypeToFields;
        this.objectTypeToEntrypoint = objectTypeToEntrypoint;
    }

    @Override
    public boolean containsGraphQLObjectType(final String objectTypeName) {
        return objectTypeToFields.containsKey(objectTypeName);
    }

    @Override
    public boolean containsGraphQLField(final String objectTypeName, final String fieldName) {
        if(containsGraphQLObjectType(objectTypeName)){
            return objectTypeToFields.get(objectTypeName).containsKey(fieldName);
        }
        return false;
    }

    @Override
    public GraphQLFieldType getGraphQLFieldType(final String objectTypeName, final String fieldName) {
        if(containsGraphQLObjectType(objectTypeName) && containsGraphQLField(objectTypeName, fieldName)){
            return objectTypeToFields.get(objectTypeName).get(fieldName).getFieldType();
        }
        return null;
    }

    @Override
    public String getGraphQLFieldValueType(final String objectTypeName, final String fieldName) {
        if(containsGraphQLObjectType(objectTypeName) && containsGraphQLField(objectTypeName, fieldName)){
            return objectTypeToFields.get(objectTypeName).get(fieldName).getValueType();
        }
        return null;
    }

    @Override
    public Set<String> getGraphQLObjectTypes() {
        return objectTypeToFields.keySet();
    }

    @Override
    public GraphQLEntrypoint getEntrypoint(final String objectTypeName, final GraphQLEntrypointType fieldType) {
        if(containsGraphQLObjectType(objectTypeName) && objectTypeToEntrypoint.containsKey(objectTypeName) && 
                objectTypeToEntrypoint.get(objectTypeName).containsKey(fieldType)){
                    
            return objectTypeToEntrypoint.get(objectTypeName).get(fieldType);
        }
        return null;
    }

    @Override
    public Map<String, GraphQLField> getGraphQLObjectFields(final String objectTypeName) {
        if(containsGraphQLObjectType(objectTypeName)){
            return objectTypeToFields.get(objectTypeName);
        }
        return null;
    }

    @Override
    public String toString( ) {
        return "GraphQL schema";
    }

}
