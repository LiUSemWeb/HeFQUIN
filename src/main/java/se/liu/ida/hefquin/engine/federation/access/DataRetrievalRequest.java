package se.liu.ida.hefquin.engine.federation.access;

import java.util.Set;

import org.apache.jena.sparql.core.Var;

public interface DataRetrievalRequest
{
	/**
	 * Returns the query variables for which this data retrieval request aims
	 * to fetch data (if any). For instance, if this request is about fetching
	 * triples that match a given triple pattern, then the variables of that
	 * triple pattern would be returned. Note that the method may also return
	 * null (which would be the case for types of data retrieval requests for
	 * which this method is not applicable in any reasonable way).
	 */
	Set<Var> getExpectedVariables();
}
