package se.liu.ida.hefquin.cli;

import java.io.PrintStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.output.NullPrintStream;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.ARQ;
import org.apache.jena.query.Query;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.Context;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.cli.modules.ModEngineConfig;
import se.liu.ida.hefquin.cli.modules.ModFederation;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.queryproc.QueryProcStats;
import se.liu.ida.hefquin.engine.utils.Pair;
import se.liu.ida.hefquin.engine.utils.Stats;
import se.liu.ida.hefquin.engine.utils.StatsPrinter;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

public class RunQueryWithoutSrcSel extends CmdARQ
{
	protected final ModTime          modTime =          new ModTime();
	protected final ModQuery         modQuery =         new ModQuery();
	protected final ModFederation    modFederation =    new ModFederation();
	protected final ModResultsOut    modResults =       new ModResultsOut();
	protected final ModEngineConfig  modEngineConfig =  new ModEngineConfig();

	protected final ArgDecl argSuppressResultPrintout = new ArgDecl(ArgDecl.NoValue, "suppressResultPrintout");
	protected final ArgDecl argPrintSourceAssignment  = new ArgDecl(ArgDecl.NoValue, "printSourceAssignment");
	protected final ArgDecl argPrintLogicalPlan   = new ArgDecl(ArgDecl.NoValue, "printLogicalPlan");
	protected final ArgDecl argPrintPhysicalPlan  = new ArgDecl(ArgDecl.NoValue, "printPhysicalPlan");
	protected final ArgDecl argQueryProcStats = new ArgDecl(ArgDecl.NoValue, "printQueryProcStats");
	protected final ArgDecl argOnelineTimeStats = new ArgDecl(ArgDecl.NoValue, "printQueryProcTime");
	protected final ArgDecl argFedAccessStats = new ArgDecl(ArgDecl.NoValue, "printFedAccessStats");

	public static void main( final String... argv ) {
		new RunQueryWithoutSrcSel(argv).mainRun();
	}

	public RunQueryWithoutSrcSel( final String[] argv ) {
		super(argv);

		addModule(modEngineConfig);
		addModule(modTime);
		addModule(modResults);

		add(argSuppressResultPrintout, "--suppressResultPrintout", "Do not print out the query result");
		add(argPrintSourceAssignment, "--printSourceAssignment", "Print out the source assignment used as input to the query optimization");
		add(argPrintLogicalPlan, "--printLogicalPlan", "Print out the logical plan produced by the logical query optimization");
		add(argPrintPhysicalPlan, "--printPhysicalPlan", "Print out the physical plan produced by the physical query optimization and used for the query execution");
		add(argQueryProcStats, "--printQueryProcStats", "Print out statistics about the query execution process");
		add(argOnelineTimeStats, "--printQueryProcTime", "Print out statistics about the query processing time in one line");
		add(argFedAccessStats, "--printFedAccessStats", "Print out statistics of the federation access manager");

		addModule(modQuery);
		addModule(modFederation);
	}

	@Override
	protected String getSummary() {
		return getCommandName()+" --query=<query> --considerSPARQLEndpoint=<endpoint URI>";
	}

	@Override
	protected void exec() {
		final ExecutorService execServiceForPlanTasks = modEngineConfig.getConfig().createExecutorServiceForPlanTasks();
		final ExecutorService execServiceForFedAccess = modEngineConfig.getConfig().createExecutorServiceForFedAccess();

		final HeFQUINEngine e = new HeFQUINEngineBuilder()
				.setConfiguration( modEngineConfig.getConfig() )
				.setFederationCatalog( modFederation.getFederationCatalog() )
				.setExecutorServiceForFederationAccess(execServiceForFedAccess)
				.setExecutorServiceForPlanTasks(execServiceForPlanTasks)
				.enablePrintingOfSourceAssignments( contains(argPrintSourceAssignment) )
				.enablePrintingOfLogicalPlans( contains(argPrintLogicalPlan) )
				.enablePrintingOfPhysicalPlans( contains(argPrintPhysicalPlan) )
				.build();

		final Query query = getQuery();
		final ResultsFormat resFmt = modResults.getResultsFormat();

		modTime.startTimer();

		final PrintStream out;
		if ( contains(argSuppressResultPrintout) )
			out = NullPrintStream.NULL_PRINT_STREAM;
		else
			out = System.out;

		Pair<QueryProcStats, List<Exception>> statsAndExceptions = null;

		try {
			statsAndExceptions = e.executeQuery(query, resFmt, out);
		}
		catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( ex.getMessage() );
			ex.printStackTrace( System.err );
		}

		if (    statsAndExceptions != null
		     && statsAndExceptions.object2 != null
		     && ! statsAndExceptions.object2.isEmpty() ) {
			final int numberOfExceptions = statsAndExceptions.object2.size();
			if ( numberOfExceptions > 1 )
				System.err.println("Attention: The query result may be incomplete because the following " + numberOfExceptions + " exceptions were caught when executing the query plan.");
			else
				System.err.println("Attention: The query result may be incomplete because the following exception was caught when executing the query plan.");

			System.err.println();
			for ( int i = 0; i < numberOfExceptions; i++ ) {
				final Exception ex = statsAndExceptions.object2.get(i);
				System.err.println( (i+1) + " " + ex.getClass().getName() + ": " + ex.getMessage() );
				if ( ex.getCause() != null ) {
					System.err.println("    ... caused by the following chain of exceptions:");
					Throwable cause = ex.getCause();
					while ( cause != null ) {
						System.err.println( "       " + cause.getClass().getName() + ": " + cause.getMessage() );
						cause = cause.getCause();
					}
				}
				System.err.println();
			}
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.err.println("Time: " + modTime.timeStr(time) + " sec");
		}

		execServiceForPlanTasks.shutdownNow();
		execServiceForFedAccess.shutdownNow();

		try {
			execServiceForPlanTasks.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
			System.err.println("Terminating the thread pool for query plan tasks was interrupted." );
			ex.printStackTrace();
		}

		try {
			execServiceForFedAccess.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
			System.err.println("Terminating the thread pool for federation access was interrupted." );
			ex.printStackTrace();
		}

		if (    statsAndExceptions != null
		     && statsAndExceptions.object1 != null) {
			if ( contains(argQueryProcStats) ) {
				StatsPrinter.print(statsAndExceptions.object1, System.err, true);
			}
			if ( contains(argOnelineTimeStats) ) {
				final long overallQueryProcessingTime = statsAndExceptions.object1.getOverallQueryProcessingTime();
				final long planningTime = statsAndExceptions.object1.getPlanningTime();
				final long compilationTime = statsAndExceptions.object1.getCompilationTime();
				final long executionTime = statsAndExceptions.object1.getExecutionTime();
				final String queryProcStats = overallQueryProcessingTime + ", " + planningTime +", " + compilationTime + ", " + executionTime + "\n";
				System.out.println(queryProcStats);
			}
		}

		if ( contains(argFedAccessStats) ) {
			final Context ctxt = ARQ.getContext();
			final FederationAccessManager fedAccessMgr = ctxt.get(HeFQUINConstants.sysFederationAccessManager);
			final Stats fedAccessStats = fedAccessMgr.getStats();
			StatsPrinter.print(fedAccessStats, System.err, true);
		}
	}

	protected Query getQuery() {
		try {
			return modQuery.getQuery();
		} catch ( final NotFoundException ex ) {
			System.err.println( "Failed to load query: "+ex.getMessage() );
			throw new TerminationException(1);
		}
	}

}
