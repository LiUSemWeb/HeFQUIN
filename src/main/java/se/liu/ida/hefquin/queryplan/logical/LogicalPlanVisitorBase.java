package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpUnion;

public class LogicalPlanVisitorBase implements LogicalPlanVisitor
{
	public void visit( final LogicalOpRequest<?,?> op )        {}

	public void visit( final LogicalOpTPAdd op )             {}

	public void visit( final LogicalOpBGPAdd op )            {}

	public void visit( final LogicalOpJoin op )              {}

	public void visit( final LogicalOpUnion op )             {}

	public void visit( final LogicalOpMultiwayJoin op )      {}

	public void visit( final LogicalOpMultiwayUnion op )     {}
}
