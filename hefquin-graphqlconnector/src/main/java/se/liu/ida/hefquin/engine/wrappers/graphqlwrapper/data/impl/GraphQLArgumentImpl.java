package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import org.apache.jena.atlas.json.JsonValue;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLArgument;

public class GraphQLArgumentImpl implements GraphQLArgument {
    protected final String varName;
    protected final String argName;
    protected final JsonValue argValue;
    protected final String argDefinition;

    public GraphQLArgumentImpl(final String varName, final String argName, final JsonValue argValue, 
            final String argDefinition){
        assert varName != null;
        assert argName != null;
        assert argValue != null;
        assert argDefinition != null;

        this.varName = varName;
        this.argName = argName;
        this.argValue = argValue;
        this.argDefinition = argDefinition;
    }

    @Override
    public String getArgName(){
        return argName;
    }

    @Override
    public JsonValue getArgValue(){
        return argValue;
    }

    @Override
    public String getArgDefinition(){
        return argDefinition;
    }

    @Override
    public String getVariableName() {
        return varName;
    }

    @Override
    public String toString(){
        return argName + ":$" + varName;
    }
}
