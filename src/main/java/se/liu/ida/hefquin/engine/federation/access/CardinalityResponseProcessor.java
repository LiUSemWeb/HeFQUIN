package se.liu.ida.hefquin.engine.federation.access;

public interface CardinalityResponseProcessor extends ResponseProcessor
{
	void process( CardinalityResponse response ) throws ResponseProcessingException;

	@Override
	default void process( DataRetrievalResponse response ) throws ResponseProcessingException 
	{
		process( (CardinalityResponse) response );
	}

}
