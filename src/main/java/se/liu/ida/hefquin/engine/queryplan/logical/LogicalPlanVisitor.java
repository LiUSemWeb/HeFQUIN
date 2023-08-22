package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

public interface LogicalPlanVisitor
{
	void visit( final LogicalOpRequest<?,?> op );

	void visit( final LogicalOpTPAdd op );
	void visit( final LogicalOpBGPAdd op );
	void visit( final LogicalOpGPAdd op );

	void visit( final LogicalOpTPOptAdd op );
	void visit( final LogicalOpBGPOptAdd op );
	void visit( final LogicalOpGPOptAdd op );

	void visit( final LogicalOpJoin op );
	void visit( final LogicalOpRightJoin op );
	void visit( final LogicalOpUnion op );

	void visit( final LogicalOpMultiwayJoin op );
	void visit( final LogicalOpMultiwayLeftJoin op );
	void visit( final LogicalOpMultiwayUnion op );
	
	void visit( final LogicalOpFilter op );
	void visit( final LogicalOpLocalToGlobal op );
	void visit( final LogicalOpGlobalToLocal op );
}
