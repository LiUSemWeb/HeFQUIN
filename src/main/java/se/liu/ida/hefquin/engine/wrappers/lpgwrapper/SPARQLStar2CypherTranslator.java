package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.Node;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherVar;

import java.util.Map;

public interface SPARQLStar2CypherTranslator {

    /**
     * Translates the given Triple Pattern into a Cypher query, using a given LPG2RDFConfiguration.
     * This method returns a {@link CypherQuery object} and a SPARQL-to-Cypher variable mapping.
     * If the Triple Pattern has a shape for which the configuration-specific RDF-star view of the
     * LPG is guaranteed to obtain no matching triples, this method returns null.
     * @return
     */
    Pair<CypherQuery, Map<CypherVar, Node>> translateTriplePattern(final TriplePattern tp,
                                                                   final LPG2RDFConfiguration conf);

}
