package se.liu.ida.hefquin.engine.federation.access;

import org.apache.jena.sparql.engine.http.HttpQuery;

public interface GraphQLInterface extends DataRetrievalInterface
{
	/**
	 * Returns the URL of the HTTP endpoint of this interface.
	 */
	String getURL();

	HttpQuery createHttpRequest(GraphQLRequest req);
}
