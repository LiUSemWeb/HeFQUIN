package se.liu.ida.hefquin.engine.queryplan;

import se.liu.ida.hefquin.engine.utils.StatsProvider;

public interface ExecutableOperator extends StatsProvider
{
	@Override
	ExecutableOperatorStats getStats();
}
