package se.liu.ida.hefquin.federation.access;

import java.util.Set;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.query.TriplePattern;

public interface BindingsRestrictedTriplePatternRequest extends DataRetrievalRequest
{
	TriplePattern getTriplePattern();

	Set<SolutionMapping> getSolutionMappings();
}
