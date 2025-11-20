package se.liu.ida.hefquin.mappings.algebra;

import java.util.List;

public interface MappingRelation
{
	/**
	 * Returns the set of attributes for which the tuples in this relation
	 * have values. The returned list can be assumed to be duplicate free.
	 * <p>
	 * The order of the attributes in the returned list is relevant when
	 * accessing the values of the tuples via a cursor for this relation
	 * (see {@link MappingRelationCursor#getValueOfCurrentTuple(int)}).
	 */
	List<String> getSchema();

	MappingRelationCursor getCursor();

}
