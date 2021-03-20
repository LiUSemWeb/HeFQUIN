package se.liu.ida.hefquin.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalPlan;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.queryplan.logical.NullaryLogicalOp;

public class LogicalPlanWithNullaryRootImpl implements LogicalPlanWithNullaryRoot
{
	private final NullaryLogicalOp rootOp;

	protected LogicalPlanWithNullaryRootImpl( final NullaryLogicalOp rootOp ) {
		assert rootOp != null;
		this.rootOp = rootOp;
	}

	@Override
	public NullaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this logical plan does not have any sub-plans" );
	}

}
