package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data;

/**
 * Used to represent either a full or segmented GraphQL field path.
 */
public interface GraphQLFieldPath {

    /**
     * @return a string version of the field segment.
     */
    public String toString();
}
