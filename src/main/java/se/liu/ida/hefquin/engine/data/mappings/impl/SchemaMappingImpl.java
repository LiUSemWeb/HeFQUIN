package se.liu.ida.hefquin.engine.data.mappings.impl;

import java.util.Collections;
import java.util.Set;

import org.apache.jena.graph.Graph;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.mappings.SchemaMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public class SchemaMappingImpl implements SchemaMapping
{
	// TODO: define the interface TermMapping and implementing classes for the various options
	//protected final Map<Node, Set<TermMapping>> g2lMap = new HashMap<>();

	public SchemaMappingImpl( final Graph mappingDescription ) {
		parseMappingDescription(mappingDescription);
	}

	protected void parseMappingDescription( final Graph mappingDescription ) {
		// TODO: implement this initialization function
	}

	@Override
	public SPARQLGraphPattern applyToTriplePattern( final TriplePattern tp ) {
		// TODO: implement this function
		return tp;
	}

	@Override
	public Set<SolutionMapping> applyToSolutionMapping( final SolutionMapping sm ) {
		// TODO: implement this function
		return Collections.singleton(sm);
	}

	@Override
	public Set<SolutionMapping> applyInverseToSolutionMapping( final SolutionMapping sm ) {
		// TODO: implement this function
		return Collections.singleton(sm);
	}

}
