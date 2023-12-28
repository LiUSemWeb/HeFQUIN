package se.liu.ida.hefquin.engine;

import java.util.concurrent.ExecutorService;

import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryproc.ExecutionEngine;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanCompiler;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcessor;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.impl.QueryProcessorImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.compiler.PushBasedQueryPlanCompilerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.execution.ExecutionEngineImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.loptimizer.LogicalOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.planning.QueryPlannerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.CostModel;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.PhysicalOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.cardinality.CardinalityEstimationImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.costmodel.CostModelImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm.EvolutionaryAlgorithmQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm.TerminatedByNumberOfGenerations;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.evolutionaryAlgorithm.TerminationCriterionFactory;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.CardinalityBasedGreedyJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.CostModelBasedGreedyJoinPlanOptimizerImpl;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.DPBasedBushyJoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.DPBasedLinearJoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.JoinPlanOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.poptimizer.simple.SimpleJoinOrderingQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.impl.srcsel.ServiceClauseBasedSourcePlannerImpl;

public class HeFQUINEngineBuilder
{
	protected HeFQUINEngineConfig config                    = null;
	protected FederationCatalog fedCatalog                  = null;
	protected FederationAccessManager fedAccessMgr          = null;
	protected LogicalToPhysicalPlanConverter l2pConverter   = null;
	protected ExecutorService execServiceForFedAccess       = null;
	protected ExecutorService execService       = null;
	protected boolean printSourceAssignment   = false;
	protected boolean printLogicalPlan        = false;
	protected boolean printPhysicalPlan       = false;
	protected boolean isExperimentRun         = false;

	protected CostModel costModel;

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setConfiguration( final HeFQUINEngineConfig config ) {
		if ( config == null )
			throw new IllegalArgumentException();

		this.config = config;
		return this;
	}

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setFederationCatalog( final FederationCatalog fedCatalog ) {
		if ( fedCatalog == null )
			throw new IllegalArgumentException();

		this.fedCatalog = fedCatalog;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder setFederationAccessManager( final FederationAccessManager fedAccessMgr ) {
		this.fedAccessMgr = fedAccessMgr;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder setLogicalToPhysicalPlanConverter( final LogicalToPhysicalPlanConverter l2pConverter ) {
		this.l2pConverter = l2pConverter;
		return this;
	}

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setExecutorServiceForFederationAccess( final ExecutorService es ) {
		if ( es == null )
			throw new IllegalArgumentException();

		this.execServiceForFedAccess = es;
		return this;
	}

	/**
	 * mandatory
	 */
	public HeFQUINEngineBuilder setExecutorServiceForPlanTasks( final ExecutorService es ) {
		if ( es == null )
			throw new IllegalArgumentException();

		this.execService = es;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder enablePrintingOfSourceAssignments( final boolean printSourceAssignment ) {
		this.printSourceAssignment = printSourceAssignment;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder enablePrintingOfLogicalPlans( final boolean printLogicalPlan ) {
		this.printLogicalPlan = printLogicalPlan;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder enablePrintingOfPhysicalPlans( final boolean printPhysicalPlan ) {
		this.printPhysicalPlan = printPhysicalPlan;
		return this;
	}

	/**
	 * optional
	 */
	public HeFQUINEngineBuilder enableExperimentRun( final boolean isExperimentRun ) {
		this.isExperimentRun = isExperimentRun;
		return this;
	}

	public HeFQUINEngine build() {
		if ( config == null )
			throw new IllegalStateException("no HeFQUINEngineConfig specified");

		if ( fedCatalog == null )
			throw new IllegalStateException("no FederationCatalog specified");

		if ( execServiceForFedAccess == null )
			throw new IllegalStateException("no ExecutorService for federation access specified");

		if ( fedAccessMgr == null )
			setDefaultFederationAccessManager();

		if ( l2pConverter == null )
			setLogicalToPhysicalPlanConverter( config.createLogicalToPhysicalPlanConverter() );

		if ( execService == null )
			throw new IllegalStateException("no ExecutorService for plan tasks specified");

		final QueryProcContext ctxt = new QueryProcContextBase() {
			@Override public FederationCatalog getFederationCatalog() { return fedCatalog; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public boolean isExperimentRun() { return isExperimentRun; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return execService; }
		};

		costModel = new CostModelImpl( new CardinalityEstimationImpl(ctxt) );
		//costModel = new CostModelImpl( new MinBasedCardinalityEstimationImpl(ctxt) );

		final SourcePlanner srcPlanner = new ServiceClauseBasedSourcePlannerImpl(ctxt);
		//final SourcePlanner srcPlanner = new ExhaustiveSourcePlannerImpl(ctxt);

		final LogicalOptimizer loptimizer = new LogicalOptimizerImpl(ctxt);

		final PhysicalOptimizer poptimizer = createQueryOptimizerWithoutOptimization();
		//final PhysicalOptimizer poptimizer = createCostModelBasedGreedyJoinPlanOptimizerImpl();
		//final PhysicalOptimizer poptimizer = createCardinalityBasedGreedyJoinPlanOptimizerImpl();
		//final PhysicalOptimizer poptimizer = createDPBasedBushyJoinPlanOptimizer();
		//final PhysicalOptimizer poptimizer = createDPBasedLinearJoinPlanOptimizer();
		//final PhysicalOptimizer poptimizer = createEvolutionaryAlgorithmQueryOptimizer(ctxt);

		final QueryPlanner planner = new QueryPlannerImpl( srcPlanner,
		                                                   loptimizer,
		                                                   poptimizer,
		                                                   printSourceAssignment,
		                                                   printLogicalPlan,
		                                                   printPhysicalPlan );
		final QueryPlanCompiler compiler = new
				//IteratorBasedQueryPlanCompilerImpl(ctxt);
				//PullBasedQueryPlanCompilerImpl(ctxt);
				PushBasedQueryPlanCompilerImpl(ctxt);
		final ExecutionEngine execEngine = new ExecutionEngineImpl();

		final QueryProcessor qProc = new QueryProcessorImpl( planner, compiler, execEngine, ctxt );
		return new HeFQUINEngineImpl(fedAccessMgr, qProc);
	}


	protected void setDefaultFederationAccessManager() {
		this.fedAccessMgr = FederationAccessUtils.getDefaultFederationAccessManager(execServiceForFedAccess);
	}


	protected PhysicalOptimizer createQueryOptimizerWithoutOptimization() {
		return new PhysicalOptimizerImpl(l2pConverter);
	}

	protected PhysicalOptimizer createCostModelBasedGreedyJoinPlanOptimizerImpl() {
		final JoinPlanOptimizer joinOpt = new CostModelBasedGreedyJoinPlanOptimizerImpl(costModel);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, l2pConverter);
	}

	protected PhysicalOptimizer createCardinalityBasedGreedyJoinPlanOptimizerImpl() {
		final JoinPlanOptimizer joinOpt = new CardinalityBasedGreedyJoinPlanOptimizerImpl(fedAccessMgr);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, l2pConverter);
	}

	protected PhysicalOptimizer createDPBasedBushyJoinPlanOptimizer() {
		final JoinPlanOptimizer joinOpt = new DPBasedBushyJoinPlanOptimizer(costModel);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, l2pConverter);
	}

	protected PhysicalOptimizer createDPBasedLinearJoinPlanOptimizer() {
		final JoinPlanOptimizer joinOpt = new DPBasedLinearJoinPlanOptimizer(costModel);
		return new SimpleJoinOrderingQueryOptimizer(joinOpt, l2pConverter);
	}

	protected PhysicalOptimizer createEvolutionaryAlgorithmQueryOptimizer( final QueryProcContext ctxt ) {
		final TerminationCriterionFactory tcFactory = TerminatedByNumberOfGenerations.getFactory(20);
		return new EvolutionaryAlgorithmQueryOptimizer(l2pConverter, costModel, ctxt, 8, 2, tcFactory);
	}
//TODO: Current overall goal is to get rid of costModel in QueryProcContext interface 

	protected static abstract class QueryProcContextBase implements QueryProcContext {
		protected final CostModel costModel;

		public QueryProcContextBase() {
			costModel = new CostModelImpl( new CardinalityEstimationImpl(this) );
//			costModel = new CostModelImpl( new MinBasedCardinalityEstimationImpl(this) );
		}

		@Override public CostModel getCostModel() { return costModel; }
	}

}
