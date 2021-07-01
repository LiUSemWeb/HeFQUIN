package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

public interface SourcePlanner
{
	LogicalPlan createSourceAssignment( final Query query ) throws SourcePlanningException;
}
