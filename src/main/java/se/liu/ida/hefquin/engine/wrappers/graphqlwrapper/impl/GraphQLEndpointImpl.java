package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.impl;

import java.util.Map;
import java.util.Set;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.GraphQLInterface;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLField;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLEntrypointType;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public class GraphQLEndpointImpl implements GraphQLEndpoint {

    // Maps object type names to a map, which in turn maps field names to GraphQLField objects
    protected final Map<String, Map<String,GraphQLField>> objectTypeToFields;

    // Maps object type names to a map, which in turn maps a GraphQLEntrypointType to a GraphQLEntrypoint
    protected final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> objectTypeToEntrypoint;

    // GraphQL Interface
    protected final GraphQLInterface graphqlInterface;

    public GraphQLEndpointImpl(final Map<String, Map<String,GraphQLField>> objectTypeToFields,
            final Map<String, Map<GraphQLEntrypointType,GraphQLEntrypoint>> objectTypeToEntrypoint, 
            final GraphQLInterface graphqlInterface){
        this.objectTypeToFields = objectTypeToFields;
        this.objectTypeToEntrypoint = objectTypeToEntrypoint;
        this.graphqlInterface = graphqlInterface;
    }

    @Override
    public VocabularyMapping getVocabularyMapping() {
        return null;
    }

    @Override
    public GraphQLInterface getInterface() {
        return graphqlInterface;
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
    public void printInformation() {

        // Print object types and fields
        System.out.println("Object types and fields\n");
        for(final String typeName : objectTypeToFields.keySet()){
            System.out.println("Object type: " + typeName);
                final Map<String,GraphQLField> fields = objectTypeToFields.get(typeName);
                for(final String fieldName : fields.keySet()){
                    final GraphQLField currentField = fields.get(fieldName);
                    System.out.println("---- Field name: " + fieldName);
                    System.out.println("---- ---- Value type: " + currentField.getValueType());
                    System.out.println("---- ---- Field type: " + currentField.getFieldType());
                }
            System.out.println("\n");
        }

        System.out.println("==================================================================\n");
        System.out.println("Entrypoints\n");

        for(final String typeName : objectTypeToEntrypoint.keySet()){
            System.out.println("Entrypoint for type: " + typeName);
            final Map<GraphQLEntrypointType,GraphQLEntrypoint> entrypoints = objectTypeToEntrypoint.get(typeName);
            for(final GraphQLEntrypointType epType : entrypoints.keySet()){
                System.out.println("---- Entrypoint Type: " + epType.toString());
                System.out.println("---- Entrypoint field name: " + entrypoints.get(epType).getFieldName());
                System.out.println("---- Args:");
                final Map<String,String> argDefs = entrypoints.get(epType).getArgumentDefinitions();
                for(String argName: argDefs.keySet()){
                    System.out.println("---- ---- " + argName + " of type " + argDefs.get(argName));
                }
            }
            System.out.println("\n");
        }
        
    }
}
