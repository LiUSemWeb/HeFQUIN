package se.liu.ida.hefquin.mappings.algebra;

import org.apache.jena.graph.Node;

public interface MappingRelationCursor
{
	/**
	 * Returns the mapping relation over which this cursor iterates.
	 */
	MappingRelation getMappingRelation();

	Node getValueOfCurrentTuple( int idxOfAttribute );

	boolean hasNext();

	void advance();
}
