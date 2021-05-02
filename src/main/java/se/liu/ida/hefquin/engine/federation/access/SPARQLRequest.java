package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

public interface SPARQLRequest extends DataRetrievalRequest
{
	SPARQLGraphPattern getQueryPattern();
}
