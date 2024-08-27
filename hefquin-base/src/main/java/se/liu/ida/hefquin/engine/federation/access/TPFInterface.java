package se.liu.ida.hefquin.engine.federation.access;

public interface TPFInterface extends TriplesRetrievalInterface
{
	String createRequestURL(TPFRequest req);
}
