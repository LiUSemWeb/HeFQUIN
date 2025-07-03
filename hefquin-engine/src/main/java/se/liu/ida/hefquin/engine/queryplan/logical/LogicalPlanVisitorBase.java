package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class LogicalPlanVisitorBase implements LogicalPlanVisitor
{
	@Override
	public void visit( final LogicalOpRequest<?,?> op )      {}

	@Override
	public void visit( final LogicalOpGPAdd op )             {}

	@Override
	public void visit( final LogicalOpGPOptAdd op )          {}

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
	public void visit( final LogicalOpBind op )              {}

	@Override
	public void visit( final LogicalOpLocalToGlobal op )     {}

	@Override
	public void visit( final LogicalOpGlobalToLocal op )     {}
}
