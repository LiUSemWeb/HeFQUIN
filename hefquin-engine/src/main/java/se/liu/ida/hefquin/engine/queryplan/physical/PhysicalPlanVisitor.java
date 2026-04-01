package se.liu.ida.hefquin.engine.queryplan.physical;

import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public interface PhysicalPlanVisitor
{
	void visit( PhysicalOpRequest<?,?> op );
	void visit( PhysicalOpFixedSolMap op );

	void visit( PhysicalOpBindJoinBRTPF op );
	void visit( PhysicalOpBindJoinSPARQL op );
	void visit( PhysicalOpIndexNestedLoopsJoin op );
	void visit( PhysicalOpLookupJoinViaWrapper op );

	void visit( PhysicalOpHashJoin1 op );
	void visit( PhysicalOpHashJoin2 op );
	void visit( PhysicalOpSymmetricHashJoin op );
	void visit( PhysicalOpNaiveNestedLoopsJoin op );

	void visit( PhysicalOpParallelMultiLeftJoin op );

	void visit( PhysicalOpBinaryUnion op );
	void visit( PhysicalOpMultiwayUnion op );

	void visit( PhysicalOpFilter op );
	void visit( PhysicalOpBind op );
	void visit( PhysicalOpUnfold op );
	void visit( PhysicalOpLocalToGlobal op );
	void visit( PhysicalOpGlobalToLocal op );
	void visit( PhysicalOpDuplicateRemoval op );
	void visit( PhysicalOpProject op );
}
