package se.liu.ida.hefquin.engine.federation.access;

public interface GraphQLInterface extends DataRetrievalInterface
{
	/**
	 * Returns the URL of the HTTP endpoint of this interface.
	 */
	String getURL();
}
