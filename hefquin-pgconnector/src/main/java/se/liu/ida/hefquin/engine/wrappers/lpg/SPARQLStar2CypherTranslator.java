package se.liu.ida.hefquin.engine.wrappers.lpg;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Triple;

import se.liu.ida.hefquin.engine.wrappers.lpg.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherMatchQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.CypherUnionQuery;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.MatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpg.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpg.utils.CypherVarGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public interface SPARQLStar2CypherTranslator
{
	/**
	 * Translates the given Triple Pattern into a Cypher query, using a given LPG2RDFConfiguration.
	 * This method returns a {@link CypherQuery} object and a SPARQL-to-Cypher variable mapping.
	 * If the Triple Pattern has a shape for which the configuration-specific RDF-star view of the
	 * LPG is guaranteed to obtain no matching triples, this method returns null.
	 */
	SPARQL2CypherTranslationResult translateTriplePattern( Triple tp,
	                                                       LPG2RDFConfiguration conf );

	/**
	 * Translates a triple pattern to a Cypher query, using restricted rules based on the different
	 * boundedness properties the variables might have
	 */
	SPARQL2CypherTranslationResult translateTriplePattern( Triple tp,
	                                                       LPG2RDFConfiguration conf,
	                                                       CypherVarGenerator generator,
	                                                       Set<Node> certainNodes,
	                                                       Set<Node> certainEdgeLabels,
	                                                       Set<Node> certainNodeLabels,
	                                                       Set<Node> certainPropertyNames,
	                                                       Set<Node> certainPropertyValues );

	/**
	 * Translates each individual triple pattern in the given BGP, and then combines the individual
	 * translations into one Cypher query that represents the whole BGP.
	 * This method statically analyzes the BGP to obtain insight on the boundedness properties
	 * of the variables in the BGP to prune unuseful subqueries.
	 * @param naive if naive translation is required.
	 */
	SPARQL2CypherTranslationResult translateBGP( Set<Triple> bgp,
	                                             LPG2RDFConfiguration conf,
	                                             boolean naive );


    /**
     * Receives a {@link CypherMatchQuery} and rewrites explicit variable joins in the WHERE clause
     * as implicit joins in the MATCH clauses. Then, it removes redundant MATCH clauses.
     * e.g. query MATCH (a)-[b]->(c) MATCH (x) WHERE a=x RETURN x is rewritten as MATCH (a)-[b]->(c) RETURN a
     */
    CypherMatchQuery rewriteJoins(CypherMatchQuery query);

    /**
     * Applies the join rewriting method to each subquery of a {@link CypherUnionQuery}
     */
    default CypherUnionQuery rewriteJoins(final CypherUnionQuery query) {
        List<CypherMatchQuery> union = new ArrayList<>();
        for (final CypherMatchQuery q : query.getSubqueries()) {
            union.add(rewriteJoins(q));
        }
        return new CypherUnionQueryImpl(union);
    }

    /**
     * Receives a list of {@link MatchClause} and merges compatible clauses into longer paths.
     * e.g., if receives (x)-[a]->(y) and (z)-[b]->(y) returns (z)-[b]->(y)<-[a]-(x).
     */
    List<MatchClause> mergePaths(List<MatchClause> matchClauses);

}
