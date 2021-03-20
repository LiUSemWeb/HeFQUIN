package se.liu.ida.hefquin.query.jenaimpl;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.query.SolutionMapping;

public class JenaBasedSolutionMapping implements SolutionMapping
{
	protected final Binding jenaObj;

	public JenaBasedSolutionMapping( final Binding jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	public Binding asJenaBinding() {
		return jenaObj;
	}

	public boolean isCompatibleWith( final JenaBasedSolutionMapping other ) {
		return JenaBasedSolutionMappingUtils.compatible(this, other);
	}

}
