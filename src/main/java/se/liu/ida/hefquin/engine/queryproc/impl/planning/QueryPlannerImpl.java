package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalQueryOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningException;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;
import se.liu.ida.hefquin.engine.utils.Pair;

/**
 * Simple implementation of {@link QueryPlanner}.
 */
public class QueryPlannerImpl implements QueryPlanner
{
	protected final SourcePlanner sourcePlanner;
	protected final LogicalOptimizer loptimizer;
	protected final PhysicalQueryOptimizer poptimizer;
	protected final boolean printLogicalPlan;
	protected final boolean printPhysicalPlan;

	public QueryPlannerImpl( final SourcePlanner sourcePlanner,
	                         final LogicalOptimizer loptimizer, // may be null
	                         final PhysicalQueryOptimizer poptimizer,
	                         final boolean printLogicalPlan,
	                         final boolean printPhysicalPlan ) {
		assert sourcePlanner != null;
		assert poptimizer != null;

		this.sourcePlanner = sourcePlanner;
		this.loptimizer = loptimizer;
		this.poptimizer = poptimizer;
		this.printLogicalPlan = printLogicalPlan;
		this.printPhysicalPlan = printPhysicalPlan;
	}

	@Override
	public SourcePlanner getSourcePlanner() { return sourcePlanner; }

	@Override
	public LogicalOptimizer getLogicalOptimizer() { return loptimizer; }

	@Override
	public PhysicalQueryOptimizer getPhysicalOptimizer() { return poptimizer; }

	@Override
	public Pair<PhysicalPlan, QueryPlanningStats> createPlan( final Query query ) throws QueryPlanningException {
		final long t1 = System.currentTimeMillis();
		final Pair<LogicalPlan, SourcePlanningStats> saAndStats = sourcePlanner.createSourceAssignment(query);

		if ( printLogicalPlan ) {
			System.out.println( LogicalPlanPrinter.print(saAndStats.object1) );
		}

		final long t2 = System.currentTimeMillis();
		final LogicalPlan lp;
		if ( loptimizer != null ) {
			final boolean keepNaryOperators = true;
			lp = loptimizer.optimize(saAndStats.object1, keepNaryOperators);
		}
		else {
			lp = saAndStats.object1;
		}

		final long t3 = System.currentTimeMillis();
		final Pair<PhysicalPlan, PhysicalQueryOptimizationStats> planAndStats = poptimizer.optimize(lp);

		final long t4 = System.currentTimeMillis();

		if ( printPhysicalPlan ) {
			System.out.println( PhysicalPlanPrinter.print(planAndStats.object1) );
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
