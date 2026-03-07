package se.liu.ida.hefquin.federation.wrappers;

/**
 * This interface captures any type of wrapper that can be used to evaluate
 * SPARQL query patterns by means of query rewriting into a query language
 * supported by the wrapped federation member.
 * 
 * More specifically, such a wrapper produces the result for a given SPARQL
 * graph pattern by rewriting it into the query language of the wrapped
 * federation member, requests the result for the rewritten query, and
 * converts the retrieved result back into a SPARQL result (i.e., a sequence
 * of solution mappings).
 */
public interface RewritingWrapper extends Wrapper
{
	// TODO
}
