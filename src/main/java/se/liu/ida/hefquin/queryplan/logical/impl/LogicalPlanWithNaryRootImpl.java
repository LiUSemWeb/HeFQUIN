package se.liu.ida.hefquin.queryplan.logical.impl;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.queryplan.LogicalPlan;
import se.liu.ida.hefquin.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.queryplan.logical.NaryLogicalOp;

public class LogicalPlanWithNaryRootImpl implements LogicalPlanWithNaryRoot
{
	private final NaryLogicalOp rootOp;
	private final List<LogicalPlan> subPlans;

	public LogicalPlanWithNaryRootImpl( final NaryLogicalOp rootOp,
	                                    final List<LogicalPlan> subPlans ) {
		assert rootOp != null;
		assert subPlans != null;

		this.rootOp = rootOp;
		this.subPlans = subPlans;
	}

	@Override
	public NaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public Iterator<LogicalPlan> getSubPlans() {
		return subPlans.iterator();
	}

	@Override
	public int numberOfSubPlans() { return subPlans.size(); }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		if ( i >= subPlans.size() )
			throw new NoSuchElementException( "this logical plan does not have a " + i + "-th sub-plan" );
		else
			return subPlans.get(i);
	}

}
