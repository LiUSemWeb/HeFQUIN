package se.liu.ida.hefquin.engine.queryplan.executable.impl;

import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.utils.StatsImpl;

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
