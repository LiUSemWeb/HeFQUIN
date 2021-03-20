package se.liu.ida.hefquin.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.queryplan.physical.BinaryPhysicalOp;
import se.liu.ida.hefquin.queryplan.physical.PhysicalPlanWithBinaryRoot;

public class PhysicalPlanWithBinaryRootImpl implements PhysicalPlanWithBinaryRoot
{
	private final BinaryPhysicalOp rootOp;
	private final PhysicalPlan subPlan1;
	private final PhysicalPlan subPlan2;

	protected PhysicalPlanWithBinaryRootImpl( final BinaryPhysicalOp rootOp,
	                                          final PhysicalPlan subPlan1,
	                                          final PhysicalPlan subPlan2 ) {
		assert rootOp != null;
		assert subPlan1 != null;
		assert subPlan2 != null;

		this.rootOp = rootOp;
		this.subPlan1 = subPlan1;
		this.subPlan2 = subPlan2;
	}

	@Override
	public BinaryPhysicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public PhysicalPlan getSubPlan1() {
		return subPlan1;
	}

	@Override
	public PhysicalPlan getSubPlan2() {
		return subPlan2;
	}

	@Override
	public int numberOfSubPlans() { return 2; }

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i == 0 )
			return subPlan1;
		else if ( i == 1 )
			return subPlan2;
		else
			throw new NoSuchElementException( "this physical plan does not have a " + i + "-th sub-plan" );
	}

}
