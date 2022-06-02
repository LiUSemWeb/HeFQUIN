package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils;

import java.util.ArrayList;
import java.util.List;

import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.data.GraphQLFieldPath;

/**
 * A builder class for creating field paths used for GraphQL queries.
 */
public class GraphQLFieldPathBuilder {

    protected final List<GraphQLFieldPath> pathSegments;

    public GraphQLFieldPathBuilder(){
        this.pathSegments = new ArrayList<>();
    }

    public GraphQLFieldPathBuilder(final List<GraphQLFieldPath> pathSegments){
        this.pathSegments = pathSegments;
    }

    public void append(final GraphQLFieldPath fieldPathSegment){
        pathSegments.add(fieldPathSegment);
    }

    public String build(){
        final StringBuilder b = new StringBuilder();
        for(final GraphQLFieldPath pathSegment : pathSegments){
            b.append(pathSegment.toString());
        }
        return b.toString();
    }
}
