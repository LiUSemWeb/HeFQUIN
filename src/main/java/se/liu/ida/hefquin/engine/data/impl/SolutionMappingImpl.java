package se.liu.ida.hefquin.engine.data.impl;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;

public class SolutionMappingImpl implements SolutionMapping
{
	protected final Binding jenaObj;

	public SolutionMappingImpl( final Binding jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	@Override
	public Binding asJenaBinding() {
		return jenaObj;
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o instanceof SolutionMapping )
			return SolutionMappingUtils.equals( this, (SolutionMapping) o );
		else
			return false;
	}

	@Override
	public String toString() {
		return  jenaObj.toString();
	}

}
