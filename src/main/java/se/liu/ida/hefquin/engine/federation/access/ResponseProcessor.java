package se.liu.ida.hefquin.engine.federation.access;

public interface ResponseProcessor<RespType extends DataRetrievalResponse>
{
	void process( RespType response ) throws ResponseProcessingException;
}
