package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;

public interface QueryPlanCompiler
{
	ExecutablePlan compile( PhysicalPlan qep ) throws QueryCompilationException;
}
