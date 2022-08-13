package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import se.liu.ida.hefquin.engine.query.Query;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
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
	protected final PhysicalQueryOptimizer optimizer;
	protected final boolean printLogicalPlan;
	protected final boolean printPhysicalPlan;

	public QueryPlannerImpl( final SourcePlanner sourcePlanner,
	                         final PhysicalQueryOptimizer optimizer,
	                         final boolean printLogicalPlan,
	                         final boolean printPhysicalPlan ) {
		assert sourcePlanner != null;
		assert optimizer != null;

		this.sourcePlanner = sourcePlanner;
		this.optimizer = optimizer;
		this.printLogicalPlan = printLogicalPlan;
		this.printPhysicalPlan = printPhysicalPlan;
	}

	@Override
	public SourcePlanner getSourcePlanner() { return sourcePlanner; }

	@Override
	public PhysicalQueryOptimizer getOptimizer() { return optimizer; }

	@Override
	public Pair<PhysicalPlan, QueryPlanningStats> createPlan( final Query query ) throws QueryPlanningException {
		final long t1 = System.currentTimeMillis();
		final Pair<LogicalPlan, SourcePlanningStats> saAndStats = sourcePlanner.createSourceAssignment(query);

		if ( printLogicalPlan ) {
			System.out.println( LogicalPlanPrinter.print(saAndStats.object1) );
		}

		final long t2 = System.currentTimeMillis();
		final Pair<PhysicalPlan, PhysicalQueryOptimizationStats> planAndStats = optimizer.optimize( saAndStats.object1 );

		final long t3 = System.currentTimeMillis();

		if ( printPhysicalPlan ) {
			System.out.println( PhysicalPlanPrinter.print(planAndStats.object1) );
		}

		final QueryPlanningStats myStats = new QueryPlanningStatsImpl( t3-t1, t2-t1, t3-t2,
		                                                               saAndStats.object2,
		                                                               saAndStats.object1,
		                                                               planAndStats.object2,
		                                                               planAndStats.object1 );

		return new Pair<>(planAndStats.object1, myStats);
	}

}
