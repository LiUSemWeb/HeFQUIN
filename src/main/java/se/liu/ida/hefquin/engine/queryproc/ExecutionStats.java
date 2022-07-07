package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.utils.Stats;

public interface ExecutionStats extends Stats
{
	ExecutablePlanStats getPlanStats();
}
