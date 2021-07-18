package se.liu.ida.hefquin.engine.federation.access;

public interface StringResponseProcessor extends ResponseProcessor
{
	void process( StringResponse response ) throws ResponseProcessingException;

	@Override
	default void process( DataRetrievalResponse response ) throws ResponseProcessingException 
	{
		process( (StringResponse) response );
	}

}
