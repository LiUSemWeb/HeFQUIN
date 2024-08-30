package se.liu.ida.hefquin.base.data.impl;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;

import java.util.Objects;

public class SolutionMappingImpl implements SolutionMapping
{
	protected final Binding jenaObj;

	public SolutionMappingImpl( final Binding jenaObject ) {
		assert jenaObject != null;
		this.jenaObj = jenaObject;
	}

	/**
	 * Create a SolutionMapping object that
	 * represents the empty solution mapping.
	 */
	public SolutionMappingImpl() {
		this( Binding.builder().build() );
	}

	@Override
	public Binding asJenaBinding() {
		return jenaObj;
	}

	@Override
	public boolean equals( final Object o ){
		if ( o instanceof SolutionMapping )
			return SolutionMappingUtils.equals( this, (SolutionMapping) o );
		else
			return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(jenaObj);
	}

	@Override
	public String toString() {
		return  jenaObj.toString();
	}

}
