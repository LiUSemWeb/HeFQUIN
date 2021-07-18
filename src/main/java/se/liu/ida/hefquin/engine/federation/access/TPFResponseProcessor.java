package se.liu.ida.hefquin.engine.federation.access;

public interface TPFResponseProcessor extends ResponseProcessor
{
	void process( TPFResponse response ) throws ResponseProcessingException;

	@Override
	default void process( DataRetrievalResponse response ) throws ResponseProcessingException 
	{
		process( (TPFResponse) response );
	}

}
