package se.liu.ida.hefquin.engine.federation.access;

import java.util.Set;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.TriplePattern;

public interface BindingsRestrictedTriplePatternRequest extends DataRetrievalRequest
{
	TriplePattern getTriplePattern();

	Set<SolutionMapping> getSolutionMappings();
}
