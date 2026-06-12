package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalPlanWithoutResult;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalPlanWithoutResult;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter.LogicalPlanStage;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningException;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;

/**
 * Simple implementation of {@link QueryPlanner}.
 */
public class QueryPlannerImpl implements QueryPlanner
{
	private static final Logger log = LoggerFactory.getLogger( QueryPlannerImpl.class );

	protected final SourcePlanner sourcePlanner;
	protected final LogicalOptimizer loptimizer;
	protected final PhysicalOptimizer poptimizer;
	protected final LogicalToPhysicalPlanConverter lp2pp;
	protected final LogicalToPhysicalOpConverter lop2pop;

	public QueryPlannerImpl( final SourcePlanner sourcePlanner,
	                         final LogicalOptimizer loptimizer, // may be null
	                         final PhysicalOptimizer poptimizer,
	                         final LogicalToPhysicalPlanConverter lp2pp,
	                         final LogicalToPhysicalOpConverter lop2pop ) {
		assert sourcePlanner != null;
		assert poptimizer != null;
		assert lp2pp != null;
		assert lop2pop != null;

		this.sourcePlanner = sourcePlanner;
		this.loptimizer = loptimizer;
		this.poptimizer = poptimizer;
		this.lp2pp = lp2pp;
		this.lop2pop = lop2pop;
	}

	@Override
	public SourcePlanner getSourcePlanner() { return sourcePlanner; }

	@Override
	public LogicalOptimizer getLogicalOptimizer() { return loptimizer; }

	@Override
	public PhysicalOptimizer getPhysicalOptimizer() { return poptimizer; }

	@Override
	public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { return lp2pp; }

	@Override
	public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() { return lop2pop; }

	@Override
	public Pair<PhysicalPlan, QueryPlanningStats> createPlan( final Query query,
	                                                          final QueryProcContextExt ctx ) throws QueryPlanningException {
		log.debug("Starting source selection phase.");

		final long t1 = System.currentTimeMillis();
		final Pair<LogicalPlan, SourcePlanningStats> saAndStats = sourcePlanner.createSourceAssignment(query, ctx);

		final long t2 = System.currentTimeMillis();
		log.debug( "Source selection completed in {} ms.", (t2 - t1) );

		if ( ctx.getSourceAssignmentPrinter() != null ) {
			ctx.getSourceAssignmentPrinter().print( saAndStats.object1,
			                                        LogicalPlanStage.SOURCE_ASSIGNMENT );
		}

		log.debug( "Starting logical optimization phase." );
		final LogicalPlan lp;
		if ( loptimizer != null ) {
			final boolean keepNaryOperators = poptimizer.assumesLogicalMultiwayJoins();
			lp = loptimizer.optimize(saAndStats.object1, keepNaryOperators, ctx);
			log.debug( "Logical optimizer invoked with keepNaryOperators={}.", keepNaryOperators );
		}
		else {
			lp = saAndStats.object1;
			log.debug( "No logical optimizer configured; using source assignment directly." );
		}

		final long t3 = System.currentTimeMillis();
		log.debug( "Logical optimization completed in {} ms.", (t3 - t2) );

		if ( ctx.getLogicalPlanPrinter() != null ) {
			ctx.getLogicalPlanPrinter().print( lp, LogicalPlanStage.FINAL_LOGICAL_PLAN );
		}

		log.debug( "Starting physical optimization phase." );
		final Pair<PhysicalPlan, PhysicalOptimizationStats> planAndStats;
		if ( lp instanceof LogicalPlanWithoutResult ) {
			planAndStats = new Pair<>( PhysicalPlanWithoutResult.getInstance(),
			                           null );  // no stats
			log.debug( "Logical optimization returned a plan that produces the empty result; skipping physical optimization." );
		}
		else
			planAndStats = poptimizer.optimize(lp, ctx);

		final long t4 = System.currentTimeMillis();
		log.debug( "Physical optimization completed in {} ms.", (t4 - t3) );

		if ( ctx.getPhysicalPlanPrinter() != null ) {
			ctx.getPhysicalPlanPrinter().print( planAndStats.object1 );
		}

		final QueryPlanningStats myStats = new QueryPlanningStatsImpl( t4-t1, t2-t1, t3-t2, t4-t3,
		                                                               saAndStats.object2,
		                                                               saAndStats.object1,
		                                                               lp,
		                                                               planAndStats.object1,
		                                                               planAndStats.object2 );

		return new Pair<>(planAndStats.object1, myStats);
	}

}
