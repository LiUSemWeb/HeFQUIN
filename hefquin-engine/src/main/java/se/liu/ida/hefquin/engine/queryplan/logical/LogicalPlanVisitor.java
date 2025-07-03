package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public interface LogicalPlanVisitor
{
	void visit( final LogicalOpRequest<?,?> op );

	void visit( final LogicalOpGPAdd op );
	void visit( final LogicalOpGPOptAdd op );

	void visit( final LogicalOpJoin op );
	void visit( final LogicalOpRightJoin op );
	void visit( final LogicalOpUnion op );

	void visit( final LogicalOpMultiwayJoin op );
	void visit( final LogicalOpMultiwayLeftJoin op );
	void visit( final LogicalOpMultiwayUnion op );
	
	void visit( final LogicalOpFilter op );
	void visit( final LogicalOpBind op );
	void visit( final LogicalOpLocalToGlobal op );
	void visit( final LogicalOpGlobalToLocal op );
}
