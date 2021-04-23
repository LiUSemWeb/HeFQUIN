package se.liu.ida.hefquin.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalPlan;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.queryplan.logical.UnaryLogicalOp;

public class LogicalPlanWithUnaryRootImpl implements LogicalPlanWithUnaryRoot
{
	private final UnaryLogicalOp rootOp;
	private final LogicalPlan subPlan;

	public LogicalPlanWithUnaryRootImpl( final UnaryLogicalOp rootOp,
	                                     final LogicalPlan subPlan ) {
		assert rootOp != null;
		assert subPlan != null;

		this.rootOp = rootOp;
		this.subPlan = subPlan;
	}

	@Override
	public UnaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public LogicalPlan getSubPlan() {
		return subPlan;
	}

	@Override
	public int numberOfSubPlans() { return 1; }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan;
		else
			throw new NoSuchElementException( "this logical plan does not have a " + i + "-th sub-plan" );
	}

}
