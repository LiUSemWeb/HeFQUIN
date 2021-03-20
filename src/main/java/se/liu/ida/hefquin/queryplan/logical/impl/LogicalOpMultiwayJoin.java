package se.liu.ida.hefquin.queryplan.logical.impl;

import java.util.List;

import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanVisitor;

public class LogicalOpMultiwayJoin extends NaryLogicalOpImpl
{
	protected LogicalOpMultiwayJoin( final List<LogicalOperator> children ) {
		super(children);
	}

	public void visit( final LogicalPlanVisitor visitor ) {
		visitor.visit(this);
	}

}
