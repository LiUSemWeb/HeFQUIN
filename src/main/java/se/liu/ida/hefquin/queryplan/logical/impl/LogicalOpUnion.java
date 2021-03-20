package se.liu.ida.hefquin.queryplan.logical.impl;

import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpUnion extends BinaryLogicalOpImpl
{
	LogicalOpUnion( final LogicalOperator childOp1, final LogicalOperator childOp2 ) {
		super( childOp1, childOp2 );
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
