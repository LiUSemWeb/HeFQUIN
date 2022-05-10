package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.impl.GraphQLFieldType;

public interface GraphQLViewProperty {
    
    /**
     * @return the name of the property
     */
    public String getName();

    /**
     * @return the GraphQL value type of the property
     */
    public String getValueType();

    /**
     * @return the field type of the property (SCALAR or OBJECT)
     */
    public GraphQLFieldType getFieldType();
}
