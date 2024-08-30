package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public interface GraphQLSchema
{
    /**
     * Verifies that a GraphQL object type with the name @param objectTypeName exists
     * for the endpoint (as defined by its schema).
     */
    public boolean containsGraphQLObjectType(final String objectTypeName);

    /**
     * Verifies that a field with the name @param fieldName exists for a GraphQL object type
     * with the name @param objectTypeName in the endpoint (as defined by its schema).
     */
    public boolean containsGraphQLField(final String objectTypeName, final String fieldName);

    /**
     * @return a GraphQLFieldType enum that describes whether the field with the name @param fieldName
     * of the GraphQL object type of the name @param objectTypeName is a scalar value or an object.
     * If the parameter names provided doesn't correspond to an object type or a field for the endpoint return null.
     */
    public GraphQLFieldType getGraphQLFieldType(final String objectTypeName, final String fieldName);

    /**
     * @return the value type (String,Int,... etc.) for the field with the name
     * @param fieldName of the GraphQL object type with the name @param objectTypeName .
     * If the parameter names provided doesn't correspond to an object type or a field for the endpoint return null.
     */
    public String getGraphQLFieldValueType(final String objectTypeName, final String fieldName);

    /**
     * @return a set with the names of all the defined GraphQL object types for the endpoint.
     */
    public Set<String> getGraphQLObjectTypes();

    /**
     * @return a map of field names for the GraphQL object type with the name @param objectTypeName mapped 
     * to their respective GraphQLField objects containing information about the field.
     * If the parameter name provided doesn't correspond to an object type for the endpoint return null.
     */
    public Map<String, GraphQLField> getGraphQLObjectFields(final String objectTypeName);

    /**
     * @return a GraphQLEntrypoint object containing information about a specific field in the GraphQL "query" type.
     * @param obejectTypeName is the name of the object type the field returns and @param fieldType is an enum describing
     * whether the field returns a single object, a filtered list of objects or a list of all objects.
     * If the parameter names provided doesn't correspond to an object type or a GraphQLEntrypointType for the endpoint return null.
     */
    public GraphQLEntrypoint getEntrypoint(final String objectTypeName, final GraphQLEntrypointType fieldType);

}
