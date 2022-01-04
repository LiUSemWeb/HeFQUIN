package se.liu.ida.hefquin.engine.federation.access;

import org.apache.jena.sparql.engine.http.HttpQuery;

public interface TPFInterface extends TriplesRetrievalInterface
{
	HttpQuery createHttpRequest(TPFRequest req);
}
