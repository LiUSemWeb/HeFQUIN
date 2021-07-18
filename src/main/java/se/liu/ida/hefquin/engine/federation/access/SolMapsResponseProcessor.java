package se.liu.ida.hefquin.engine.federation.access;

public interface SolMapsResponseProcessor extends ResponseProcessor
{
	void process( SolMapsResponse response ) throws ResponseProcessingException;

	@Override
	default void process( DataRetrievalResponse response ) throws ResponseProcessingException 
	{
		process( (SolMapsResponse) response );
	}

}
