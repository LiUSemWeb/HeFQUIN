package se.liu.ida.hefquin.mappings.algebra;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.Node_Ext;
import org.apache.jena.shared.PrefixMapping;

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


	/**
	 * To be used as the error symbol in mapping relations.
	 */
	static Node errorNode = new Node_Ext<Integer>(0) {
		@Override
		public String toString() { return "error symbol"; }

		@Override
		public String toString( final PrefixMapping pmap ) {
			throw new UnsupportedOperationException();
		}
	};

	static String sAttr = "s";
	static String pAttr = "p";
	static String oAttr = "o";
	static String gAttr = "g";
}
