package se.liu.ida.hefquin.federation.access;

public interface DataRetrievalInterface
{
	boolean supportsTriplePatternRequests();

	boolean supportsBGPRequests();

	boolean supportsSPARQLPatternRequests();

	boolean supportsRequest( final DataRetrievalRequest req );
	
	/**
	 * Returns an identifier of this data retrieval interface (federation member), which should be unique.
	 */
	int getID();
}
