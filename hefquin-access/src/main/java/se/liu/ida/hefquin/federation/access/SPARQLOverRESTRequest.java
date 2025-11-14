package se.liu.ida.hefquin.federation.access;

import java.util.List;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

/**
 * This interface represents a request to evaluate a SPARQL graph pattern
 * over an RDF view of data obtained from an arbitrary REST endpoint; hence,
 * performing this request would involve issuing a REST request to the REST
 * endpoint.
 * <p>
 * Such a request is meant to be performed in the context of a solution
 * mapping that has bindings for the variables in {@link #getParamVars()}.
 * The values bound to these variables will be used as query parameters in
 * the REST request to be issued.
 */
public interface SPARQLOverRESTRequest extends DataRetrievalRequest
{
	/**
	 * Returns the graph pattern for which solutions are requested.
	 */
	SPARQLGraphPattern getQueryPattern();

	/**
	 * Returns the variables via which the query parameters for the REST
	 * request will be obtained from the solution mapping in which context
	 * this request is performed. This method may return {@code null} in
	 * case the REST request is meant to be formed without query parameters.
	 */
	List<Var> getParamVars();
}
