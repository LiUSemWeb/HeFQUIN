package se.liu.ida.hefquin.mappings.algebra.sources;

import java.util.List;

import org.apache.jena.graph.Node;

import se.liu.ida.hefquin.base.query.Query;

public interface SourceType< DDS extends DataObject,
                             DC1 extends DataObject,
                             DC2 extends DataObject,
                             QL1 extends Query,
                             QL2 extends Query >
{
	boolean isRelevantDataObject( DataObject d );
	List<DC1> eval( QL1 query, DDS input );
	List<DC2> eval( QL2 query, DDS input, DC1 cxtObj );
	Node cast( DC2 d );
}
