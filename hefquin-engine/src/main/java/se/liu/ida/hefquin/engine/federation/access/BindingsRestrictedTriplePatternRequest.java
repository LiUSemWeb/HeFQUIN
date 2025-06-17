package se.liu.ida.hefquin.engine.federation.access;

import java.util.Set;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.query.TriplePattern;

public interface BindingsRestrictedTriplePatternRequest extends DataRetrievalRequest
{
	TriplePattern getTriplePattern();

	Set<Binding> getSolutionMappings();
}
