package se.liu.ida.hefquin.federation.access;

public interface TPFInterface extends TriplesRetrievalInterface
{
	String createRequestURL(TPFRequest req);
}
