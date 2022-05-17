package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import java.util.Map;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;

public class GraphQLEntrypointImpl implements GraphQLEntrypoint {

    protected final String fieldName;
    protected final Map<String,String> parameterDefs;
    protected final String type;

    public GraphQLEntrypointImpl(final String fieldName, final Map<String,String> parameterDefs,
            final String type){
        this.fieldName = fieldName;
        this.parameterDefs = parameterDefs;
        this.type = type;
    }

    @Override
    public String getFieldName() {
        return this.fieldName;
    }

    @Override
    public Map<String, String> getParameterDefinitions() {
        return parameterDefs;
    }

    @Override
    public String getType() {
        return type;
    }
}
