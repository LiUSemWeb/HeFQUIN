package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.ExecutablePlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;

public interface QueryPlanner
{
	Pair<PhysicalPlan, QueryPlanningStats> createPlan( Query query,
	                                                   QueryProcContextExt ctx )
			throws QueryPlanningException;

	SourcePlanner getSourcePlanner();

	LogicalOptimizer getLogicalOptimizer();

	PhysicalOptimizer getPhysicalOptimizer();

	LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter();
	LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter();

	ExecutablePlanPrinter getExecutablePlanPrinter();
}
