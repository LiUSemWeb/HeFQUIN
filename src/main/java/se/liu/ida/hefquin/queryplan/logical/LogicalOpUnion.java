package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public class LogicalOpUnion extends BinaryLogicalOpImpl
{
	LogicalOpUnion( final LogicalOperator childOp1, final LogicalOperator childOp2 ) {
		super( childOp1, childOp2 );
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
