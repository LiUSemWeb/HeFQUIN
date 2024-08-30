package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;

public interface ExecutionStats extends Stats
{
	ExecutablePlanStats getPlanStats();
}
