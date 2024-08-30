package se.liu.ida.hefquin.base.data;

public interface Triple
{
	/**
	 * Returns a representation of this triple as an object of the
	 * class {@link org.apache.jena.graph.Triple} of the Jena API.
	 */
	org.apache.jena.graph.Triple asJenaTriple();
}
