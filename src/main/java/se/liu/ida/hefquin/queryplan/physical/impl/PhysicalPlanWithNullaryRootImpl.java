package se.liu.ida.hefquin.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.queryplan.physical.NullaryPhysicalOp;
import se.liu.ida.hefquin.queryplan.physical.PhysicalPlanWithNullaryRoot;

public class PhysicalPlanWithNullaryRootImpl implements PhysicalPlanWithNullaryRoot
{
	private final NullaryPhysicalOp rootOp;

	public PhysicalPlanWithNullaryRootImpl( final NullaryPhysicalOp rootOp ) {
		assert rootOp != null;
		this.rootOp = rootOp;
	}

	@Override
	public NullaryPhysicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this physical plan does not have any sub-plan" );
	}

}
