package se.liu.ida.hefquin.queryplan.logical;

import java.util.List;

import se.liu.ida.hefquin.queryplan.LogicalOperator;

public class LogicalOpMultiwayJoin extends NaryLogicalOpImpl
{
	protected LogicalOpMultiwayJoin( final List<LogicalOperator> children ) {
		super(children);
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
