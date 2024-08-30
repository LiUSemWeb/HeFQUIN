package se.liu.ida.hefquin.engine.wrappers.graphql.data.impl;

import java.util.HashSet;
import java.util.Set;

import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLArgument;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLEntrypoint;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLFieldPath;

public class GraphQLEntrypointPath implements GraphQLFieldPath {

    protected final GraphQLEntrypoint ep;
    protected final int uniqueNr;
    protected final Set<GraphQLArgument> epArgs;

    public GraphQLEntrypointPath(final GraphQLEntrypoint ep, final int uniqueNr,
            final Set<GraphQLArgument> epArgs) {

        assert ep != null;
        assert epArgs != null;

        this.ep = ep;
        this.uniqueNr = uniqueNr;
        this.epArgs = epArgs;
    }

    public GraphQLEntrypointPath(final GraphQLEntrypoint ep, final int uniqueNr) {
        this.ep = ep;
        this.uniqueNr = uniqueNr;
        this.epArgs = new HashSet<>();
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();
        b.append(ep.getEntrypointAlias(uniqueNr));

        if(!epArgs.isEmpty()){
            b.append("(");
            for(final GraphQLArgument arg : epArgs){
                b.append(arg);
                b.append(",");
            }
            b.deleteCharAt(b.length()-1);
            b.append(")");
        }

        b.append("/");

        return b.toString();
    }
}
