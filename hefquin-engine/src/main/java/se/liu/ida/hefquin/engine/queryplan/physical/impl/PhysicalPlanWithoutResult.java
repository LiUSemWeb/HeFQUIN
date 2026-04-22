package se.liu.ida.hefquin.engine.queryplan.physical.impl;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithoutResult;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

/**
 * This class represents a physical plan that produces the empty result.
 */
public class PhysicalPlanWithoutResult extends BaseForQueryPlan
                                       implements PhysicalPlan
{
	public static PhysicalPlan getInstance() { return instance; }

	protected static final PhysicalPlan instance = new PhysicalPlanWithoutResult();

	protected PhysicalPlanWithoutResult() {}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public ExpectedVariables getExpectedVariables() {
		return LogicalPlanWithoutResult.expVars;
	}

	@Override
	public PhysicalOperator getRootOperator() throws NoSuchElementException {
		throw new NoSuchElementException();
	}

	@Override
	public PhysicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException();
	}
}
