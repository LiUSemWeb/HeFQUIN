package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

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
	public boolean equals( final Object o ) {
		if ( ! (o instanceof LogicalPlanWithNaryRoot) )
			return false; 

		final LogicalPlanWithNaryRoot oo = (LogicalPlanWithNaryRoot) o;
		if ( oo == this )
			return true;

		if ( oo.numberOfSubPlans() != subPlans.size() )
			return false;

		if ( ! oo.getRootOperator().equals(rootOp) )
			return false;

		final Iterator<LogicalPlan> it1 = subPlans.iterator();
		final Iterator<LogicalPlan> it2 = oo.getSubPlans();
		while ( it1.hasNext() ) {
			if ( ! it1.next().equals(it2.next()) )
				return false;
		}

		return true;
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
