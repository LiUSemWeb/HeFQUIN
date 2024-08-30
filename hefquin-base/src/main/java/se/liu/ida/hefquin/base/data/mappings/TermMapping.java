package se.liu.ida.hefquin.base.data.mappings;

import java.util.Set;

import org.apache.jena.graph.Node;

public interface TermMapping
{
	/**
	 * Get the type of the mapping rule, e.g., equivalentClass,
	 * equivalentProperty, subClassOf, subPropertyOf, unionOf
	 */
	Node getTypeOfRule();

	/**
	 * Return the global term that is mapped by this mapping.
	 */
	Node getGlobalTerm();

	/**
	 * Return the local terms that this mapping maps to.
	 */
	Set<Node> getLocalTerms();
}
