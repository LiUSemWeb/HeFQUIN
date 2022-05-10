package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import java.util.TreeMap;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLEntrypoint;

public class GraphQLEntrypointImpl implements GraphQLEntrypoint {

    protected final String fieldName;
    protected final TreeMap<String,String> parameterDefs;
    protected final String type;

    public GraphQLEntrypointImpl(final String fieldName, final TreeMap<String,String> parameterDefs,
            final String type){
        this.fieldName = fieldName;
        this.parameterDefs = parameterDefs;
        this.type = type;
    }

    @Override
    public final String getFieldName() {
        return this.fieldName;
    }

    @Override
    public TreeMap<String, String> getParameterDefinitions() {
        return parameterDefs;
    }

    @Override
    public String getType() {
        return type;
    }
}
