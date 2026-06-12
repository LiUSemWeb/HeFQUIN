package se.liu.ida.hefquin.engine.queryproc;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.queryplan.utils.ExecutablePlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public interface QueryProcContext
{
	FederationCatalog getFederationCatalog();

	FederationAccessManager getFederationAccessMgr();

	ExecutorService getExecutorServiceForPlanTasks();

	/**
	 * A plan printer to be used for printing the source assignment (which
	 * is the initial logical plan after source selection, before logical
	 * optimization). If source assignment printing is not requested by
	 * the user, then this method returns <code>null</code>.
	 */
	LogicalPlanPrinter getSourceAssignmentPrinter();

	/**
	 * A plan printer to be used for printing the final logical plan that is
	 * the result of logical optimization. If logical plan printing is not
	 * requested by the user, then this method returns <code>null</code>.
	 */
	LogicalPlanPrinter getLogicalPlanPrinter();

	/**
	 * A plan printer to be used for printing the final physical plan that
	 * is the result of physical optimization. If physical plan printing is
	 * not requested by the user, then this method returns <code>null</code>.
	 */
	PhysicalPlanPrinter getPhysicalPlanPrinter();

	/**
	 * A plan printer to be used for printing the executable plan, as
	 * produced from the final physical plan. If executable plan printing
	 * is not requested by the user, this method returns <code>null</code>.
	 */
	ExecutablePlanPrinter getExecutablePlanPrinter();

	/**
	 * Returns <code>true</code> if the query execution process is done as
	 * part of an experiment, in which case additional statistics need to
	 * be produced.
	 */
	boolean isExperimentRun();

	/**
	 * Returns <code>true</code> if the user requested to skip the actual
	 * query execution. In this case, the query processor can stop after
	 * query planning.
	 */
	boolean skipExecution();
}
