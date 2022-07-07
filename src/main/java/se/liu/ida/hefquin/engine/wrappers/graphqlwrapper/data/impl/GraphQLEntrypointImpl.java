package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import java.util.Map;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;

public class GraphQLEntrypointImpl implements GraphQLEntrypoint {

    // Name of the query field
    protected final String fieldName;

    // Arguments for the query field mapped as names to their GraphQL definitions (including non-null identifier)
    protected final Map<String,String> argumentDefinitions;

    // Name of the GraphQL type the query field fetches
    protected final String typeName;

    // Enum describing whether the data fetched is a single object, list of filtered objects or a list of all objects
    protected final GraphQLEntrypointType epType;

    public GraphQLEntrypointImpl(final String fieldName, final Map<String,String> argumentDefinitions,
            final String typeName, final GraphQLEntrypointType epType){

        assert fieldName != null;
        assert argumentDefinitions != null;
        assert typeName != null;
        assert epType != null;
        
        this.fieldName = fieldName;
        this.argumentDefinitions = argumentDefinitions;
        this.typeName = typeName;
        this.epType = epType;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public Map<String, String> getArgumentDefinitions() {
        return argumentDefinitions;
    }

    @Override
    public String getTypeName() {
        return typeName;
    }

    @Override
    public String getEntrypointAlias(int counter) {
        return "ep_" + epType.toString().toLowerCase() + counter + ":" + fieldName;
    }
}
