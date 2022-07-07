package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;

public interface QueryPlanCompiler
{
	ExecutablePlan compile( PhysicalPlan qep ) throws QueryCompilationException;
}
