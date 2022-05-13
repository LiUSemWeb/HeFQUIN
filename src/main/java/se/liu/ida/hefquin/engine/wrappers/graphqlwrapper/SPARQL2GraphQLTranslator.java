package se.liu.ida.hefquin.engine.wrappers.graphqlwrapper;

import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.wrappers.graphqlwrapper.query.GraphQLQuery;

/**
 * Translator for SPARQL to GraphQL
 */
public interface SPARQL2GraphQLTranslator {

    /**
     * Translates @param bgp into a GraphQL query using information from @param config
     */
    public GraphQLQuery translateBGP(final BGP bgp, final GraphQL2RDFConfiguration config);
}
