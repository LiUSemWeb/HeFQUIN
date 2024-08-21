package se.liu.ida.hefquin.engine.federation.access;

public interface DataRetrievalInterface
{
	boolean supportsTriplePatternRequests();

	boolean supportsBGPRequests();

	boolean supportsSPARQLPatternRequests();

	boolean supportsRequest( final DataRetrievalRequest req );
	
	int getID();
}
