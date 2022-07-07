package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;

public interface QueryPlanCompiler
{
	ExecutablePlan compile( PhysicalPlan qep ) throws QueryCompilationException;
}
