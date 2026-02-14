package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.NullaryLogicalOp;

public class LogicalPlanWithNullaryRootImpl extends BaseForQueryPlan
                                            implements LogicalPlanWithNullaryRoot
{
	private final NullaryLogicalOp rootOp;

	public LogicalPlanWithNullaryRootImpl( final NullaryLogicalOp rootOp ) {
		assert rootOp != null;
		this.rootOp = rootOp;
	}

	@Override
	public NullaryLogicalOp getRootOperator() {
		return rootOp;
	}

	@Override
	public ExpectedVariables getExpectedVariables() {
		return rootOp.getExpectedVariables();
	}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException( "this logical plan does not have any sub-plans" );
	}

}
