package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.queryplan.logical.NaryLogicalOp;

public class LogicalOpMultiwayUnion implements NaryLogicalOp
{
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
