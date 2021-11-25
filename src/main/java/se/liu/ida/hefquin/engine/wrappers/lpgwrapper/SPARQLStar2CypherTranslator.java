package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;

public interface SPARQLStar2CypherTranslator {

    /**
     * Translates the given Triple Pattern into a Cypher query, using a given LPG2RDFConfiguration.
     * If the Triple Pattern is incompatible with the configuration, this method returns null.
     */
    CypherQuery translateTriplePattern(final TriplePattern tp, final LPG2RDFConfiguration conf);
}
