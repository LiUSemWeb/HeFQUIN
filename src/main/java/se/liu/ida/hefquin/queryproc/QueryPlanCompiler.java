package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.queryplan.PhysicalPlan;

public interface QueryPlanCompiler
{
	ExecutablePlan compile( final PhysicalPlan qep );
}
