package se.liu.ida.hefquin.queryproc;

import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.LogicalPlan;

public interface SourcePlanner
{
	LogicalPlan createSourceAssignment( final Query query );
}
