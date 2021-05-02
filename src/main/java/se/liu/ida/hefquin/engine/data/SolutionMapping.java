package se.liu.ida.hefquin.engine.data;

import org.apache.jena.sparql.engine.binding.Binding;

public interface SolutionMapping
{
	/**
	 * Returns a representation of this solution mapping as an
	 * object of the class {@link Binding} of the Jena API.
	 */
	Binding asJenaBinding();
}
