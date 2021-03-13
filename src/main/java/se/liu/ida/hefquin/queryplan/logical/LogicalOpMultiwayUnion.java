package se.liu.ida.hefquin.queryplan.logical;

import java.util.List;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public class LogicalOpMultiwayUnion extends NaryLogicalOpImpl
{
	protected LogicalOpMultiwayUnion( final List<LogicalOperator> children ) {
		super(children);
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
