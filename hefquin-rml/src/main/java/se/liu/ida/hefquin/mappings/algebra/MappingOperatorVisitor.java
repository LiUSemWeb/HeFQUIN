package se.liu.ida.hefquin.mappings.algebra;

import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpConstant;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtract;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;

public interface MappingOperatorVisitor
{
	void visit( MappingOpConstant op );
	void visit( MappingOpExtract<?,?,?,?,?> op );
	void visit( MappingOpExtend op );
	void visit( MappingOpProject op );
	void visit( MappingOpJoin op );
	void visit( MappingOpUnion op );
}
