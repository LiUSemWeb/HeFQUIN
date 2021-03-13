package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.query.SPARQLGraphPattern;

public interface SPARQLRequest extends DataRetrievalRequest
{
	SPARQLGraphPattern getQueryPattern();
}
