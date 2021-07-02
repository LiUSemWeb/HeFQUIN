package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.NaryPhysicalOp;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWithNaryRoot;

public class PhysicalPlanWithNaryRootImpl implements PhysicalPlanWithNaryRoot
{
	protected final NaryPhysicalOp rootOp;
	protected final List<PhysicalPlan> subPlans;

	public PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                     final PhysicalPlan... subPlans ) {
		this( rootOp, Arrays.asList(subPlans) );
	}

	public PhysicalPlanWithNaryRootImpl( final NaryPhysicalOp rootOp,
	                                     final List<PhysicalPlan> subPlans ) {
		assert rootOp != null;
		assert subPlans != null;
		assert ! subPlans.isEmpty();

		this.rootOp = rootOp;
		this.subPlans = subPlans;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		final ExpectedVariables[] e = new ExpectedVariables[ subPlans.size() ];
		for ( int i = 0; i < subPlans.size(); ++i ) {
			e[i] = subPlans.get(i).getExpectedVariables();
		}
		return rootOp.getExpectedVariables(e);
	}

	@Override
	public int numberOfSubPlans() {
		return subPlans.size();
	}

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		return subPlans.get(i);
	}

	@Override
	public NaryPhysicalOp getRootOperator() {
		return rootOp;
	}

}
