package se.liu.ida.hefquin.engine.queryplan.logical.impl;

import java.util.NoSuchElementException;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.base.impl.BaseForQueryPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

/**
 * This class represents a logical plan that produces the empty result.
 */
public class LogicalPlanWithoutResult extends BaseForQueryPlan
                                      implements LogicalPlan
{
	public static LogicalPlan getInstance() { return instance; }

	protected static final LogicalPlan instance = new LogicalPlanWithoutResult();

	public static final ExpectedVariables expVars = new ExpectedVariables() {
		@Override
		public Set<Var> getCertainVariables() { return Set.of(); }

		@Override
		public Set<Var> getPossibleVariables() { return Set.of(); }
	};

	protected LogicalPlanWithoutResult() {}

	@Override
	public int numberOfSubPlans() { return 0; }

	@Override
	public ExpectedVariables getExpectedVariables() { return expVars; }

	@Override
	public LogicalOperator getRootOperator() throws NoSuchElementException {
		throw new NoSuchElementException();
	}

	@Override
	public LogicalPlan getSubPlan( final int i ) throws NoSuchElementException {
		throw new NoSuchElementException();
	}
}
