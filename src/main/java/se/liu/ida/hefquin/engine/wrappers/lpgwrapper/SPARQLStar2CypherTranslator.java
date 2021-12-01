package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;

public interface SPARQLStar2CypherTranslator {

    /**
     * Translates the given Triple Pattern into a Cypher query, using a given LPG2RDFConfiguration.
     * If the Triple Pattern has a shape for which the configuration-specific RDF-star view of the
     * LPG is guaranteed to obtain no matching triples, this method returns null.
     */
    CypherQuery translateTriplePattern(final TriplePattern tp, final LPG2RDFConfiguration conf);
}