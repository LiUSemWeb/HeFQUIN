package se.liu.ida.hefquin.engine.federation.access;

public interface CardinalityResponse extends DataRetrievalResponse<Integer>
{
	int getCardinality();
}
