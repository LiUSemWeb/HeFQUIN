package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherVarGenerator;

import java.util.Map;
import java.util.Set;

public interface SPARQLStar2CypherTranslator {

    /**
     * Translates the given Triple Pattern into a Cypher query, using a given LPG2RDFConfiguration.
     * This method returns a {@link CypherQuery} object and a SPARQL-to-Cypher variable mapping.
     * If the Triple Pattern has a shape for which the configuration-specific RDF-star view of the
     * LPG is guaranteed to obtain no matching triples, this method returns null.
     */
    Pair<CypherQuery, Map<CypherVar, Var>> translateTriplePattern(final TriplePattern tp,
                                                                   final LPG2RDFConfiguration conf);

    /**
     * Translates a triple pattern to a Cypher query, using restricted rules based on the different
     * boundedness properties the variables might have
     */
    Pair<CypherQuery, Map<CypherVar, Var>> translateTriplePattern(final TriplePattern tp,
                                                                  final LPG2RDFConfiguration conf,
                                                                  final CypherVarGenerator generator,
                                                                  final Set<Node> certainNodes,
                                                                  final Set<Node> certainEdgeLabels,
                                                                  final Set<Node> certainNodeLabels,
                                                                  final Set<Node> certainPropertyNames,
                                                                  final Set<Node> certainPropertyValues);

    /**
     * Translates each individual triple pattern in the given BGP, and then combines the individual
     * translations into one Cypher query that represents the whole BGP.
     * This method statically analyzes the BGP to obtain insight on the boundedness properties
     * of the variables in the BGP to prune unuseful subqueries.
     */
    Pair<CypherQuery, Map<CypherVar, Var>> translateBGP(final BGP bgp, final LPG2RDFConfiguration conf);

}
