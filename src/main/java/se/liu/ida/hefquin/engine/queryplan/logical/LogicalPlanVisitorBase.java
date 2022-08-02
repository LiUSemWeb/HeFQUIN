package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
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

public class LogicalPlanVisitorBase implements LogicalPlanVisitor
{
	@Override
	public void visit( final LogicalOpRequest<?,?> op )      {}

	@Override
	public void visit( final LogicalOpTPAdd op )             {}

	@Override
	public void visit( final LogicalOpBGPAdd op )            {}

	@Override
	public void visit( final LogicalOpTPOptAdd op )          {}

	@Override
	public void visit( final LogicalOpBGPOptAdd op )         {}

	@Override
	public void visit( final LogicalOpJoin op )              {}

	@Override
	public void visit( final LogicalOpRightJoin op )         {}

	@Override
	public void visit( final LogicalOpUnion op )             {}

	@Override
	public void visit( final LogicalOpMultiwayJoin op )      {}

	@Override
	public void visit( final LogicalOpMultiwayLeftJoin op )  {}

	@Override
	public void visit( final LogicalOpMultiwayUnion op )     {}

	@Override
	public void visit( final LogicalOpFilter op )            {}

	@Override
	public void visit( final LogicalOpLocalToGlobal op )     {}

	@Override
	public void visit( final LogicalOpGlobalToLocal op )     {}
}
