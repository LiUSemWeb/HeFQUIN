package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.engine.queryplan.physical.UnaryPhysicalOp;

public class PhysicalPlanWithUnaryRootImpl implements PhysicalPlanWithUnaryRoot
{
	private final UnaryPhysicalOp rootOp;
	private final PhysicalPlan subPlan;

	public PhysicalPlanWithUnaryRootImpl( final UnaryPhysicalOp rootOp,
	                                      final PhysicalPlan subPlan ) {
		assert rootOp != null;
		assert subPlan != null;

		this.rootOp = rootOp;
		this.subPlan = subPlan;
	}

	@Override
	public UnaryPhysicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public PhysicalPlan getSubPlan() {
		return subPlan;
	}

	@Override
	public int numberOfSubPlans() { return 1; }

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan;
		else
			throw new NoSuchElementException( "this physical plan does not have a " + i + "-th sub-plan" );
	}

}
