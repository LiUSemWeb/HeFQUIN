package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public interface PhysicalPlanVisitor
{
	void visit( PhysicalOpRequest<?,?> op );
	void visit( PhysicalOpRequestWithTranslation<?,?> op );

	void visit( PhysicalOpBindJoin op );
	void visit( PhysicalOpBindJoinWithVALUES op );
	void visit( PhysicalOpBindJoinWithUNION op );
	void visit( PhysicalOpBindJoinWithFILTER op );
	void visit( PhysicalOpBindJoinWithFILTERandTranslation op );
	void visit( PhysicalOpNaiveNestedLoopsJoin op );
	void visit( PhysicalOpIndexNestedLoopsJoin op );

	void visit( PhysicalOpParallelMultiLeftJoin op );

	void visit( PhysicalOpHashJoin op );
	void visit( PhysicalOpSymmetricHashJoin op );

	void visit( PhysicalOpHashRJoin op );

	void visit( PhysicalOpBinaryUnion op );

	void visit( PhysicalOpFilter op );
	void visit( PhysicalOpLocalToGlobal op );
	void visit( PhysicalOpGlobalToLocal op );
}
