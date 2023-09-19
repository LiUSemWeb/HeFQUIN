package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class LogicalPlanVisitorBase implements LogicalPlanVisitor
{
	@Override
	public void visit( final LogicalOpRequest<?,?> op )      {}

	@Override
	public void visit( final LogicalOpTPAdd op )             {}

	@Override
	public void visit( final LogicalOpBGPAdd op )            {}

	@Override
	public void visit( final LogicalOpGPAdd op )             {}

	@Override
	public void visit( final LogicalOpTPOptAdd op )          {}

	@Override
	public void visit( final LogicalOpBGPOptAdd op )         {}

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
	public void visit( final LogicalOpExtend op )            {}

	@Override
	public void visit( final LogicalOpLocalToGlobal op )     {}

	@Override
	public void visit( final LogicalOpGlobalToLocal op )     {}
}
