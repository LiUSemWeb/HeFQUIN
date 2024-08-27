package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.utils.QueryTranslatingException;

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
        final GraphQLEndpoint endpoint) throws QueryTranslatingException;
}
