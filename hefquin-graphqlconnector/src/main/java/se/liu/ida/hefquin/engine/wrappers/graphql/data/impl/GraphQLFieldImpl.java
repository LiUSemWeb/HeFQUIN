package se.liu.ida.hefquin.engine.wrappers.graphql.data.impl;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLField;

public class GraphQLFieldImpl implements GraphQLField {
    
    protected final String name;
    protected final String valueType;
    protected final GraphQLFieldType fieldType;

    public GraphQLFieldImpl(final String name,
            final String valueType, final GraphQLFieldType fieldType){

        assert name != null;
        assert valueType != null;
        assert fieldType != null;

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
