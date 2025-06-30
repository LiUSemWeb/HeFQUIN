package se.liu.ida.hefquin.federation.access;

public interface SPARQLEndpointInterface extends SolMapRetrievalInterface
{
	/** Returns the URL of the HTTP endpoint of this interface. */
	String getURL();
}
