package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.base.query.TriplePattern;

public interface TriplePatternRequest extends SPARQLRequest
{
	@Override
	TriplePattern getQueryPattern();
}
