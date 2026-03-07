package se.liu.ida.hefquin.cli;

import java.io.PrintStream;
import java.util.List;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.Query;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.resultset.ResultsFormat;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModResultsOut;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.base.utils.StatsPrinter;
import se.liu.ida.hefquin.cli.modules.ModEngineConfig;
import se.liu.ida.hefquin.cli.modules.ModFederation;
import se.liu.ida.hefquin.cli.modules.ModPlanPrinting;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;

/**
 * A command-line tool that executes SPARQL queries using the HeFQUIN federation
 * engine without source selection. This class extends {@code CmdARQ} for query
 * processing, execution, and result handling within the HeFQUIN system. It also
 * supports printing various statistics about query execution and federation
 * access.
 */
public class RunQueryWithoutSrcSel extends CmdARQ
{
	protected final ModTime          modTime =          new ModTime();
	protected final ModQuery         modQuery =         new ModQuery();
	protected final ModFederation    modFederation =    new ModFederation();
	protected final ModPlanPrinting  modPlanPrinting =  new ModPlanPrinting();
	protected final ModResultsOut    modResults =       new ModResultsOut();
	protected final ModEngineConfig  modEngineConfig =  new ModEngineConfig();

	protected final ArgDecl argSuppressResultPrintout = new ArgDecl( ArgDecl.NoValue, "suppressResultPrintout" );
	protected final ArgDecl argSkipExecution = new ArgDecl( ArgDecl.NoValue, "skipExecution" );
	protected final ArgDecl argQueryProcStats = new ArgDecl( ArgDecl.NoValue, "printQueryProcStats" );
	protected final ArgDecl argOnelineTimeStats = new ArgDecl( ArgDecl.NoValue, "printQueryProcMeasurements" );
	protected final ArgDecl argFedAccessStats = new ArgDecl( ArgDecl.NoValue, "printFedAccessStats" );

	/**
	 * Main entry point of the tool, accepting command-line arguments to specify the
	 * query, configuration, and output format.
	 *
	 * @param argv Command-line arguments.
	 */
	public static void main( final String[] argv ) {
		new RunQueryWithoutSrcSel( argv ).mainRun();
	}

	/**
	 * Constructor that initializes the command-line tool with necessary argument
	 * modules for speciffying, e.g., federation configuration, engine configuration, and output format.
	 *
	 * @param argv Command-line arguments.
	 */
	public RunQueryWithoutSrcSel( final String[] argv ) {
		super( argv );

		addModule( modTime );
		addModule( modPlanPrinting );
		addModule( modResults );

		add( argSuppressResultPrintout, "--suppressResultPrintout", "Do not print out the query result" );
		add( argSkipExecution, "--skipExecution", "Do not execute the query (but create the execution plan)" );
		add( argQueryProcStats, "--printQueryProcStats", "Print out statistics about the query execution process" );
		add( argOnelineTimeStats, "--printQueryProcMeasurements",
				"Print out measurements about the query processing time in one line that can be used for a CSV file" );
		add( argFedAccessStats, "--printFedAccessStats", "Print out statistics of the federation access manager" );

		addModule( modQuery );
		addModule( modEngineConfig );
		addModule( modFederation );
	}

	/**
	 * Returns the usage summary string of the command, showing the required arguments.
	 *
	 * @return A string that describes the usage of the command.
	 */
	@Override
	protected String getSummary() {
		return getCommandName() + " --query=<query> --federationDescription=<federation description>";
	}


	/**
	 * Returns the command name used to invoke the tool.
	 *
	 * @return The name of the command.
	 */
	@Override
	protected String getCommandName() {
		return "hefquin";
	}

	/**
	 * Executes the query using the HeFQUIN federation engine and handles the
	 * results and statistics.
	 */
	@Override
	protected void exec() {
		final HeFQUINEngineBuilder builder = new HeFQUINEngineBuilder()
			.withFederationCatalog( modFederation.getFederationCatalog() )
			.withSourceAssignmentPrinter( modPlanPrinting.getSourceAssignmentPrinter() )
			.withLogicalPlanPrinter( modPlanPrinting.getLogicalPlanPrinter() )
			.withPhysicalPlanPrinter( modPlanPrinting.getPhysicalPlanPrinter() )
			.withExecutablePlanPrinter( modPlanPrinting.getExecutablePlanPrinter() )
			.setSkipExecution( contains(argSkipExecution) );

		if( modEngineConfig.getConfDescr() != null ){
			builder.withEngineConfiguration( modEngineConfig.getConfDescr() );
		}

		final HeFQUINEngine e = builder.build();

		final Query query = getQuery();
		final ResultsFormat resFmt = modResults.getResultsFormat();

		modTime.startTimer();

		final PrintStream out;
		if ( contains( argSuppressResultPrintout ) ) {
			out = NullPrintStream.INSTANCE;
		} else {
			out = System.out;
		}

		QueryProcessingStatsAndExceptions statsAndExceptions = null;

		try {
			statsAndExceptions = e.executeQueryAndPrintResult(query, resFmt, out);
		}
		catch ( final IllegalQueryException ex ) {
			System.out.flush();
			System.err.println( "The given query is invalid:" );
			System.err.println( ex.getMessage() );
		}
		catch ( final UnsupportedQueryException ex ) {
			System.out.flush();
			System.err.println( "The given query is not supported by HeFQUIN:" );
			System.err.println( ex.getMessage() );
		}
		catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( ex.getMessage() );
			ex.printStackTrace( System.err );
		}

		if ( statsAndExceptions != null && statsAndExceptions.containsExceptions() ) {
			final List<Exception> exceptions = statsAndExceptions.getExceptions();
			final int numberOfExceptions = exceptions.size();
			if ( numberOfExceptions > 1 ) {
				System.err.println( "Attention: The query result may be incomplete because the following "
						+ numberOfExceptions + " exceptions were caught when executing the query plan." );
			} else {
				System.err.println( "Attention: The query result may be incomplete because the following "
						+ "exception was caught when executing the query plan" );
			}

			System.err.println();
			for ( int i = 0; i < numberOfExceptions; i++ ) {
				final Exception ex = exceptions.get(i);
				final Throwable rc = getRootCause( ex );
				System.err.println( (i + 1) + " " + rc.getClass().getName() + ": " + rc.getMessage() );
				System.err.println( "StackTrace:" );
				ex.printStackTrace( System.err );
				System.err.println();
			}
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.err.println( "Time: " + modTime.timeStr( time ) + " sec" );
		}

		e.shutdown();

		if ( statsAndExceptions != null ) {
			if ( contains(argQueryProcStats) ) {
				StatsPrinter.print( statsAndExceptions, System.err, true );
				System.err.println();
			}
			if ( contains(argOnelineTimeStats) ) {
				final long overallQueryProcessingTime = statsAndExceptions.getOverallQueryProcessingTime();
				final long planningTime = statsAndExceptions.getPlanningTime();
				final long compilationTime = statsAndExceptions.getCompilationTime();
				final long executionTime = statsAndExceptions.getExecutionTime();
				final String queryProcStats = overallQueryProcessingTime + ", " + planningTime + ", " + compilationTime
						+ ", " + executionTime;
				System.out.println( queryProcStats );
			}
		}

		if ( contains(argFedAccessStats) ) {
			final Stats fedAccessStats = e.getFederationAccessStats();
			StatsPrinter.print( fedAccessStats, System.err, true );
			System.err.println();
		}
	}

    /**
     * Rturns the SPARQL query to be executed.
     *
     * @return the {@code Query} object
     * @throws TerminationException if the query file could not be found
     */
	protected Query getQuery() {
		try {
			return modQuery.getQuery();
		} catch ( final NotFoundException ex ) {
			System.err.println( "Failed to load query: " + ex.getMessage() );
			throw new TerminationException( 1 );
		}
	}

	/**
	 * Returns the root cause of a throwable by traversing the cause chain.
	 *
	 * This method follows the chain of {@code Throwable.getCause()} until it
	 * reaches the deepest non-null cause. If the input {@code throwable} has no
	 * cause, the method returns the throwable itself.
	 *
	 * @param throwable the throwable from which to extract the root cause
	 * @return the root cause of the throwable, or {@code null} if {@code throwable}
	 *         is {@code null}
	 */
	private static Throwable getRootCause( final Throwable throwable ) {
		Throwable cause = throwable;
		while ( cause.getCause() != null ) {
			cause = cause.getCause();
		}
		return cause;
	}
}
