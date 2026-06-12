package se.liu.ida.hefquin.engine.queryproc;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.queryplan.utils.ExecutablePlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class QueryProcContextBuilder
{
	protected final FederationCatalog fedCatalog;
	protected final FederationAccessManager fedAccMgr;
	protected final ExecutorService execServiceForPlanTasks;

	protected LogicalPlanPrinter srcasgPrinter    = null;
	protected LogicalPlanPrinter lplanPrinter     = null;
	protected PhysicalPlanPrinter pplanPrinter    = null;
	protected ExecutablePlanPrinter eplanPrinter  = null;

	protected boolean isExperimentRun  = false;
	protected boolean skipExecution    = false;

	public QueryProcContextBuilder( final FederationCatalog fedCatalog,
	                                final FederationAccessManager fedAccMgr,
	                                final ExecutorService execServiceForPlanTasks ) {
		assert fedCatalog != null;
		assert fedAccMgr != null;
		assert execServiceForPlanTasks != null;

		this.fedCatalog = fedCatalog;
		this.fedAccMgr = fedAccMgr;
		this.execServiceForPlanTasks = execServiceForPlanTasks;
	}

	/**
	 * Sets the source assignment printer to be used within the query
	 * processing context.
	 *
	 * @param p - a logical-plan printer to be used for printing a source
	 *            assignment that is the input to logical plan optimization;
	 *            this argument may be <code>null</code>, which means that
	 *            source assignment printing will be skipped in the created
	 *            query processing context (for this case, it is also possible
	 *            to omit calling this method at all)
	 * @return this builder instance for method chaining
	 */
	public QueryProcContextBuilder setSourceAssignmentPrinter( final LogicalPlanPrinter p ) {
		srcasgPrinter = p;
		return this;
	}

	/**
	 * Sets the logical-plan printer to be used within the query processing
	 * context.
	 *
	 * @param p - a logical-plan printer to be used for printing the logical
	 *            plan after logical plan optimization; this argument may be
	 *            <code>null</code>, which means that logical-plan printing
	 *            will be skipped in the created query processing context
	 *            (for this case, it is also possible to omit calling this
	 *            method at all)
	 * @return this builder instance for method chaining
	 */
	public QueryProcContextBuilder setLogicalPlanPrinter( final LogicalPlanPrinter p ) {
		lplanPrinter = p;
		return this;
	}

	/**
	 * Sets the physical-plan printer to be used within the query processing
	 * context.
	 *
	 * @param p - a physical-plan printer to be used for printing the
	 *            physical plan after physical plan optimization; this
	 *            argument may be <code>null</code>, which means that
	 *            physical-plan printing will be skipped in the created
	 *            query processing context (for this case, it is also
	 *            possible to omit calling this method at all)
	 * @return this builder instance for method chaining
	 */
	public QueryProcContextBuilder setPhysicalPlanPrinter( final PhysicalPlanPrinter p ) {
		pplanPrinter = p;
		return this;
	}

	/**
	 * Sets the executable-plan printer to be used within the query
	 * processing context.
	 *
	 * @param p - an executable-plan printer to be used for printing the
	 *            executable plan before executing it; this argument may
	 *            be <code>null</code>, which means that executable-plan
	 *            printing will be skipped in the created query processing
	 *            context (for this case, it is also possible to omit
	 *            calling this method at all)
	 * @return this builder instance for method chaining
	 */
	public QueryProcContextBuilder setExecutablePlanPrinter( final ExecutablePlanPrinter p ) {
		eplanPrinter = p;
		return this;
	}

	public QueryProcContextBuilder setIsExperimentRun( final boolean set ) {
		isExperimentRun = set;
		return this;
	}

	public QueryProcContextBuilder setSkipExecution( final boolean set ) {
		skipExecution = set;
		return this;
	}

	public QueryProcContext build() {
		return new QueryProcContext() {
			@Override
			public FederationCatalog getFederationCatalog() {
				return fedCatalog;
			}

			@Override
			public FederationAccessManager getFederationAccessMgr() {
				return fedAccMgr;
			}

			@Override
			public ExecutorService getExecutorServiceForPlanTasks() {
				return execServiceForPlanTasks;
			}

			@Override
			public LogicalPlanPrinter getSourceAssignmentPrinter() {
				return srcasgPrinter;
			}

			@Override
			public LogicalPlanPrinter getLogicalPlanPrinter() {
				return lplanPrinter;
			}

			@Override
			public PhysicalPlanPrinter getPhysicalPlanPrinter() {
				return pplanPrinter;
			}

			@Override
			public ExecutablePlanPrinter getExecutablePlanPrinter() {
				return eplanPrinter;
			}

			@Override
			public boolean skipExecution() {
				return skipExecution;
			}

			@Override
			public boolean isExperimentRun() {
				return isExperimentRun;
			}
		};
	}
}
