package se.liu.ida.hefquin.cli;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;
import org.apache.commons.io.output.NullPrintStream;
import org.apache.jena.cmd.TerminationException;
import org.apache.jena.query.Query;
import org.apache.jena.shared.NotFoundException;
import org.apache.jena.sparql.resultset.ResultsFormat;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.cli.modules.ModEngineConfig;
import se.liu.ida.hefquin.cli.modules.ModFederation;
import se.liu.ida.hefquin.cli.modules.ModPlanPrinting;
import se.liu.ida.hefquin.cli.modules.ModQuery;
import se.liu.ida.hefquin.cli.modules.ModResultsOutExt;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContext;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextBuilder;

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
	protected final ModResultsOutExt modResultsExt =    new ModResultsOutExt();
	protected final ModEngineConfig  modEngineConfig =  new ModEngineConfig();

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
	 * modules for specifying, e.g., federation configuration, engine configuration, and output format.
	 *
	 * @param argv Command-line arguments.
	 */
	public RunQueryWithoutSrcSel( final String[] argv ) {
		super( argv );

		addModule( modTime );
		addModule( modEngineConfig );
		addModule( modPlanPrinting );
		addModule( modResultsExt );

		addModule( modQuery );
		addModule( modFederation );
	}

	/**
	 * Returns the usage summary string of the command, showing the required arguments.
	 *
	 * @return A string that describes the usage of the command.
	 */
	@Override
	protected String getSummary() {
		return getCommandName() + " --query <query> --fd <federation description>";
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

	@Override
	protected void processModulesAndArgs() {
		super.processModulesAndArgs();

		// Fix because 'ModGeneral' currently sets the verbose flag instead
		// of the debug flag whenever the --debug argument is given.
		if ( isVerbose() )
			modGeneral.debug = true;
	}

	/**
	 * Executes the query using the HeFQUIN federation engine and handles the
	 * results and statistics.
	 */
	@Override
	protected void exec() {
		final HeFQUINEngineBuilder builder = new HeFQUINEngineBuilder()
			.withFederationCatalogInModels( modFederation.getFederationCatalog() );

		if( modEngineConfig.getConfDescr() != null ){
			builder.withEngineConfiguration( modEngineConfig.getConfDescr() );
		}

		final HeFQUINEngine e = builder.build();

		final Query query = getQuery();
		final ResultsFormat resFmt = modResultsExt.getResultsFormat();

		final PrintStream out;
		final PrintStream ownedStream;
		// Result printout suppression has highest precedence
		if ( modResultsExt.isSuppressResultPrintout() ) {
			out = NullPrintStream.INSTANCE;
			ownedStream = null;
		}
		else if ( modResultsExt.getOutputFile() != null ) {
			final String filename = modResultsExt.getOutputFile();
			try {
				ownedStream = new PrintStream(
						new FileOutputStream(filename, true), // append to file
						true ); // auto-flush
				out = ownedStream;
			}
			catch ( final FileNotFoundException ex ) {
				cmdError( "Failed to create print stream for output destination: " + filename, false );
				return;
			}
		}
		else {
			out = System.out;
			ownedStream = null;
		}

		final QueryProcContextBuilder ctxBuilder = e.getQueryProcContextBuilder()
					.setSourceAssignmentPrinter( modPlanPrinting.getSourceAssignmentPrinter() )
					.setLogicalPlanPrinter( modPlanPrinting.getLogicalPlanPrinter() )
					.setPhysicalPlanPrinter( modPlanPrinting.getPhysicalPlanPrinter() )
					.setExecutablePlanPrinter( modPlanPrinting.getExecutablePlanPrinter() )
					.setSkipExecution( modResultsExt.isSkipExecution() );
		final QueryProcContext ctx = ctxBuilder.build();

		modTime.startTimer();

		QueryProcessingStatsAndExceptions statsAndExceptions = null;
		try {
			statsAndExceptions = e.executeQueryAndPrintResult(query, resFmt, out, ctx);
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

		if ( ownedStream != null )
			ownedStream.close();

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
				System.err.println( "Exception " + (i + 1) + ": " + ex.getMessage() );
				if ( isDebug() ) {
					System.err.println( "StackTrace:" );
					ex.printStackTrace( System.err );
				}
				System.err.println();
			}
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.err.println( "Time: " + modTime.timeStr( time ) + " sec" );
		}

		e.shutdown();

		if ( statsAndExceptions != null ) {
			modResultsExt.handleQueryProcStats( statsAndExceptions, msg -> cmdError( msg, false ) );

			modResultsExt.handleOnelineTimeStats( extractOnelineTimeStats( statsAndExceptions ), msg -> cmdError( msg, false ) );
		}

		modResultsExt.handleFedAccessStats( e.getFederationAccessStats(), msg -> cmdError( msg, false ) );
	}

    /**
     * Returns the SPARQL query to be executed.
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
	 * Extracts and formats query processing statistics from the given
	 * {@code QueryProcessingStatsAndExceptions} object into a comma-separated string.
	 *
	 * The returned string contains the overall query processing time, planning time,
	 * compilation time, and execution time, in that order.
	 *
	 * @param statsAndExceptions the object containing query processing statistics
	 * @return a comma-separated string of query processing statistics
	 */
	private static String extractOnelineTimeStats( final QueryProcessingStatsAndExceptions statsAndExceptions ) {
		final long overallQueryProcessingTime = statsAndExceptions.getOverallQueryProcessingTime();
		final long planningTime = statsAndExceptions.getPlanningTime();
		final long compilationTime = statsAndExceptions.getCompilationTime();
		final long executionTime = statsAndExceptions.getExecutionTime();
		final String queryProcStats = overallQueryProcessingTime + ", " + planningTime + ", " + compilationTime
				+ ", " + executionTime;
		return queryProcStats;
	}
}
