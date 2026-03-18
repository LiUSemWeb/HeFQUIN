package se.liu.ida.hefquin.engine.queryproc.impl.planning;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import se.liu.ida.hefquin.base.query.Query;
import se.liu.ida.hefquin.base.utils.Pair;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.utils.ExecutablePlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryproc.LogicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizationStats;
import se.liu.ida.hefquin.engine.queryproc.PhysicalOptimizer;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanner;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningException;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanner;
import se.liu.ida.hefquin.engine.queryproc.SourcePlanningStats;

/**
 * Simple implementation of {@link QueryPlanner}.
 */
public class QueryPlannerImpl implements QueryPlanner
{
	protected final SourcePlanner sourcePlanner;
	protected final LogicalOptimizer loptimizer;
	protected final PhysicalOptimizer poptimizer;
	protected final LogicalPlanPrinter srcasgPrinter;
	protected final LogicalPlanPrinter lplanPrinter;
	protected final PhysicalPlanPrinter pplanPrinter;
	protected final ExecutablePlanPrinter eplanPrinter;

	public QueryPlannerImpl( final SourcePlanner sourcePlanner,
	                         final LogicalOptimizer loptimizer, // may be null
	                         final PhysicalOptimizer poptimizer,
	                         final LogicalPlanPrinter srcasgPrinter,     // may be null
	                         final LogicalPlanPrinter lplanPrinter,      // may be null
	                         final PhysicalPlanPrinter pplanPrinter,     // may be null
	                         final ExecutablePlanPrinter eplanPrinter) { // may be null{
		assert sourcePlanner != null;
		assert poptimizer != null;

		this.sourcePlanner = sourcePlanner;
		this.loptimizer = loptimizer;
		this.poptimizer = poptimizer;
		this.srcasgPrinter = srcasgPrinter;
		this.lplanPrinter = lplanPrinter;
		this.pplanPrinter = pplanPrinter;
		this.eplanPrinter = eplanPrinter;
	}

	@Override
	public SourcePlanner getSourcePlanner() { return sourcePlanner; }

	@Override
	public LogicalOptimizer getLogicalOptimizer() { return loptimizer; }

	@Override
	public PhysicalOptimizer getPhysicalOptimizer() { return poptimizer; }

	@Override
	public Pair<PhysicalPlan, QueryPlanningStats> createPlan( final Query query,
	                                                          final QueryProcContext ctxt ) throws QueryPlanningException {
		final long t1 = System.currentTimeMillis();
		final Pair<LogicalPlan, SourcePlanningStats> saAndStats = sourcePlanner.createSourceAssignment(query, ctxt);

		if ( srcasgPrinter != null ) {
			final String srcasgPrinterPath = srcasgPrinter.getFileOutputPath();
			if ( srcasgPrinterPath != null ) {
				try {
					srcasgPrinter.print( saAndStats.object1, new PrintStream( new FileOutputStream(srcasgPrinterPath, true) ) );
				} catch ( final FileNotFoundException ex ) {
					System.err.println( "Error: Could not create file for printing source assignment: " + srcasgPrinterPath );
				}
			}
			else {
				System.out.println("--------- Source Assignment ---------");
				srcasgPrinter.print( saAndStats.object1, System.out );
			}
		}
		
		final long t2 = System.currentTimeMillis();
		final LogicalPlan lp;
		if ( loptimizer != null ) {
			final boolean keepNaryOperators = poptimizer.assumesLogicalMultiwayJoins();
			lp = loptimizer.optimize(saAndStats.object1, keepNaryOperators, ctxt);
		}
		else {
			lp = saAndStats.object1;
		}
		
		if ( lplanPrinter != null ) {
			final String lplanPrinterPath = lplanPrinter.getFileOutputPath();
			if ( lplanPrinterPath != null ) {
				try {
					lplanPrinter.print( lp, new PrintStream( new FileOutputStream(lplanPrinterPath, true) ) );
				} catch ( final FileNotFoundException ex ) {
					System.err.println( "Error: Could not create file for printing logical plan: " + lplanPrinterPath );
				}
			}
			else {
				System.out.println("--------- Logical Plan ---------");
				lplanPrinter.print( lp, System.out );
			}
		}

		final long t3 = System.currentTimeMillis();
		final Pair<PhysicalPlan, PhysicalOptimizationStats> planAndStats = poptimizer.optimize(lp, ctxt);

		final long t4 = System.currentTimeMillis();

		if ( pplanPrinter != null ) {
			final String pplanPrinterPath = pplanPrinter.getFileOutputPath();
			if ( pplanPrinterPath != null ) {
				try {
					pplanPrinter.print( planAndStats.object1, new PrintStream( new FileOutputStream(pplanPrinterPath, true) ) );
				} catch ( final FileNotFoundException ex ) {
					System.err.println( "Error: Could not create file for printing physical plan: " + pplanPrinterPath );
					System.err.println( "Printing physical plan to standard output instead." );
					pplanPrinter.print( planAndStats.object1, System.out );
				}
			}
			else {
				System.out.println("--------- Physical Plan ---------");
				pplanPrinter.print( planAndStats.object1, System.out );
			}
		}

		final QueryPlanningStats myStats = new QueryPlanningStatsImpl( t4-t1, t2-t1, t3-t2, t4-t3,
		                                                               saAndStats.object2,
		                                                               saAndStats.object1,
		                                                               lp,
		                                                               planAndStats.object1,
		                                                               planAndStats.object2 );

		return new Pair<>(planAndStats.object1, myStats);
	}

	@Override
	public ExecutablePlanPrinter getExecutablePlanPrinter() {
		return eplanPrinter;
	}

	// @Override
	// public String getExecutablePlanPrinterPath() {
	// 	return eplanFilePath;
	// }
}
