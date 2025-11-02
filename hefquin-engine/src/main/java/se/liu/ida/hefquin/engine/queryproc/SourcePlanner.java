package se.liu.ida.hefquin.engine.queryproc;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

public interface SourcePlanner
{
	/**
	 * The logical plan returned by this method is a source assignment (see
	 * Definition 8 of our FedQPL paper); that is, the only types of operators
	 * that it contains are request operators ({@link LogicalOpRequest}),
	 * multiway joins ({@link LogicalOpMultiwayJoin}), and multiway unions
	 * ({@link LogicalOpMultiwayUnion}). 
	 */
	Pair<LogicalPlan, SourcePlanningStats> createSourceAssignment( Query query,
	                                                               QueryProcContext ctxt ) throws SourcePlanningException;
}
