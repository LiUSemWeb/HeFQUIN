package se.liu.ida.hefquin.engine.federation.access;

import org.apache.jena.sparql.engine.http.HttpQuery;

public interface BRTPFInterface extends TPFInterface
{
	HttpQuery createHttpRequest(BRTPFRequest req);
}
