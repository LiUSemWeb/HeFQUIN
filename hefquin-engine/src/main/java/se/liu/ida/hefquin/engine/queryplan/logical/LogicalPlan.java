package se.liu.ida.hefquin.engine.queryplan.logical;

import java.util.NoSuchElementException;

import se.liu.ida.hefquin.engine.queryplan.base.QueryPlan;

public interface LogicalPlan extends QueryPlan
{
	@Override
	LogicalOperator getRootOperator();

	@Override
	LogicalPlan getSubPlan( int i ) throws NoSuchElementException;
}
