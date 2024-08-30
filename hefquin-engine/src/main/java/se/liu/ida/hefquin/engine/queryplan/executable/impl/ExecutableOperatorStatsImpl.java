package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.base.utils.StatsImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;

public class ExecutableOperatorStatsImpl extends StatsImpl implements ExecutableOperatorStats
{
	protected static final String enClassName    = "classNameOfOperator";
	protected static final String enShortString  = "shortStringOfOperator";

	public ExecutableOperatorStatsImpl( final ExecutableOperator op ) {
		put( enClassName,   op.getClass().getSimpleName() );
		put( enShortString, op.toString() );
	}

	@Override
	public String getClassName() {
		return (String) getEntry(enClassName);
	}

	@Override
	public String getShortString() {
		return (String) getEntry(enShortString);
	}
}
