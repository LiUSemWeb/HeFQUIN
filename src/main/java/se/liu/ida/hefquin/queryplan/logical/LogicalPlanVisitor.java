package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpUnion;

public interface LogicalPlanVisitor
{
	void visit( final LogicalOpRequest<?,?> op );

	void visit( final LogicalOpTPAdd op );
	void visit( final LogicalOpBGPAdd op );

	void visit( final LogicalOpJoin op );
	void visit( final LogicalOpUnion op );

	void visit( final LogicalOpMultiwayJoin op );
	void visit( final LogicalOpMultiwayUnion op );
}
