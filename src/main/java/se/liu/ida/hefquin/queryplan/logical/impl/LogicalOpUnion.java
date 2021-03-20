package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.queryplan.logical.BinaryLogicalOp;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpUnion implements BinaryLogicalOp
{
	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
