package se.liu.ida.hefquin.engine.wrappers.lpgwrapper;

import java.util.Map;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

/**
 * Objects of this type capture the output of the SPARQL-to-Cypher
 * translation (as performed by a {@link SPARQLStar2CypherTranslator}).
 * Such output consists of the resulting Cypher query and a mapping from
 * each variable used in this Cypher query to the SPARQL variable that
 * the Cypher variables corresponds to.
 */
public interface SPARQL2CypherTranslationResult
{
	/**
	 * Returns the Cypher query produced by the translation process.
	 */
	CypherQuery getCypherQuery();

	/**
	 * Returns the mapping that maps each variable introduced in the Cypher
	 * query (see {@link #getCypherQuery()}) to the corresponding SPARQL
	 * variable in the SPARQL pattern that was translated.
	 */
	Map<CypherVar, Var> getVariablesMapping();
}
