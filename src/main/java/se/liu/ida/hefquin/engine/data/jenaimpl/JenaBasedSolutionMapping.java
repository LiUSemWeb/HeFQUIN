package se.liu.ida.hefquin.engine.data.jenaimpl;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

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
