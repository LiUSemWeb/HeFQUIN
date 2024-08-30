package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.query.TriplePattern;

public interface TriplePatternRequest extends SPARQLRequest
{
	@Override
	TriplePattern getQueryPattern();
}
