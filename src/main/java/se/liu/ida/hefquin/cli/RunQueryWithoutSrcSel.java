package se.liu.ida.hefquin.cli;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

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

	protected final ArgDecl argPrintLogicalPlan = new ArgDecl(ArgDecl.NoValue, "printLogicalPlan");
	protected final ArgDecl argPrintPhysicalPlan = new ArgDecl(ArgDecl.NoValue, "printPhysicalPlan");
	protected final ArgDecl argQueryProcStats = new ArgDecl(ArgDecl.NoValue, "queryProcStats");
	protected final ArgDecl argFedAccessStats = new ArgDecl(ArgDecl.NoValue, "fedAccessStats");

	public static void main( final String... argv ) {
		new RunQueryWithoutSrcSel(argv).mainRun();
	}

	public RunQueryWithoutSrcSel( final String[] argv ) {
		super(argv);

		addModule(modTime);
		addModule(modQuery);
		addModule(modFederation);
		addModule(modResults);
		addModule(modEngineConfig);

		add(argPrintLogicalPlan, "--printLogicalPlan", "Print out the logical plan used for the query optimization");
		add(argPrintPhysicalPlan, "--printPhysicalPlan", "Print out the physical plan used for the query execution");
		add(argQueryProcStats, "--queryProcStats", "Print out statistics about the query execution process");
		add(argFedAccessStats, "--fedAccessStats", "Print out statistics of the federation access manager");
	}

	@Override
	protected String getSummary() {
		return getCommandName()+" --query=<query>";
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
				.enablePrintingOfLogicalPlans( contains(argPrintLogicalPlan) )
				.enablePrintingOfPhysicalPlans( contains(argPrintPhysicalPlan) )
				.build();

		final Query query = getQuery();
		final ResultsFormat resFmt = modResults.getResultsFormat();

		modTime.startTimer();

		QueryProcStats queryProcStats = null;

		try {
			queryProcStats = e.executeQuery(query, resFmt);
		}
		catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( ex.getMessage() );
			ex.printStackTrace( System.err );
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

		if ( queryProcStats != null && contains(argQueryProcStats) ) {
			StatsPrinter.print(queryProcStats, System.err, true);
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
