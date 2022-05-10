package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLViewProperty;

public class GraphQLViewPropertyImpl implements GraphQLViewProperty {
    
    protected final String name;
    protected final String valueType;
    protected final GraphQLFieldType fieldType;

    public GraphQLViewPropertyImpl(final String name,
            final String valueType, final GraphQLFieldType fieldType){
        this.name = name;
        this.valueType = valueType;
        this.fieldType = fieldType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValueType() {
        return valueType;
    }

    @Override
    public GraphQLFieldType getFieldType() {
        return fieldType;
    }
}
