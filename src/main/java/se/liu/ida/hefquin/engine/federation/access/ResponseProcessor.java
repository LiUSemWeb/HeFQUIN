package se.liu.ida.hefquin.engine.federation.access;

public interface ResponseProcessor
{
	void process( DataRetrievalResponse response ) throws ResponseProcessingException;
}
