package se.liu.ida.hefquin.base.query;

public interface SPARQLQuery extends Query
{
	/**
	 * Returns a representation of this SPARQL query as an object of the
	 * {@link org.apache.jena.query.Query} class of the Jena API.
	 */
	org.apache.jena.query.Query asJenaQuery();
}
