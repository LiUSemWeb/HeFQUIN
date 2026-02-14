package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.NaryLogicalOp;

public class LogicalPlanWithNaryRootImpl extends BaseForQueryPlan
                                         implements LogicalPlanWithNaryRoot
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

	public LogicalPlanWithNaryRootImpl( final NaryLogicalOp rootOp,
	                                    final LogicalPlan ... subPlans ) {
		assert rootOp != null;
		assert subPlans != null;

		this.rootOp = rootOp;
		this.subPlans = Arrays.asList(subPlans);
	}

	@Override
	public NaryLogicalOp getRootOperator() {
		return rootOp;
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
