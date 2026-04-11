package se.liu.ida.hefquin.mappings.algebra;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpConstant;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.sources.DataObject;

public interface MappingOperatorVisitor
{
	<DDS extends DataObject,
	 DC1 extends DataObject,
	 DC2 extends DataObject,
	 QL1 extends Query,
	 QL2 extends Query>
	void visit( MappingOpExtract<DDS, DC1, DC2, QL1, QL2> op );

	void visit( MappingOpConstant op );

	void visit( MappingOpExtend op );
	void visit( MappingOpProject op );

	void visit( MappingOpJoin op );
	void visit( MappingOpUnion op );
}
