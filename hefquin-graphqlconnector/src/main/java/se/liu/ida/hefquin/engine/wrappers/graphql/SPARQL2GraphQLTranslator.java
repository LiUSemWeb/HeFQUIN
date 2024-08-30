package se.liu.ida.hefquin.engine.wrappers.graphql;

import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.engine.wrappers.graphql.data.GraphQLSchema;
import se.liu.ida.hefquin.engine.wrappers.graphql.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphql.utils.QueryTranslatingException;

/**
 * Translator for SPARQL to GraphQL
 */
public interface SPARQL2GraphQLTranslator {

    /**
     * Translates @param bgp into a GraphQL query using information 
     * from @param config and @param endpoint
     * @throws QueryTranslatingException if there was some problem translating the query.
     */
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config,
        final GraphQLSchema schema) throws QueryTranslatingException;
}
