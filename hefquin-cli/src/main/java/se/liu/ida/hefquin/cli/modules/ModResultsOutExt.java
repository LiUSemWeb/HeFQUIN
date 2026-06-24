package se.liu.ida.hefquin.cli.modules;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.function.Consumer;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;

import arq.cmdline.ModResultsOut;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.base.utils.StatsPrinter;

/**
 * Command-line argument module for specifying result output,
 * query processing statistics and federation access statistics.
 */
public class ModResultsOutExt extends ModResultsOut
{
	protected final ArgDecl argSuppressResultPrintout = new ArgDecl( ArgDecl.NoValue, "suppressResultPrintout" );
	protected final ArgDecl argSkipExecution          = new ArgDecl( ArgDecl.NoValue, "skipExecution" );
	protected final ArgDecl argQueryProcStats         = new ArgDecl( ArgDecl.NoValue, "printQueryProcStats" );
	protected final ArgDecl argOnelineTimeStats       = new ArgDecl( ArgDecl.NoValue, "printQueryProcMeasurements" );
	protected final ArgDecl argFedAccessStats         = new ArgDecl( ArgDecl.NoValue, "printFedAccessStats" );
	protected final ArgDecl argQueryProcStatsToFile   = new ArgDecl( ArgDecl.HasValue, "printQueryProcStatsToFile" );
	protected final ArgDecl argOnelineTimeStatsToFile = new ArgDecl( ArgDecl.HasValue, "printQueryProcMeasurementsToFile" );
	protected final ArgDecl argFedAccessStatsToFile   = new ArgDecl( ArgDecl.HasValue, "printFedAccessStatsToFile" );

	protected boolean suppressResultPrintout;
	protected boolean skipExecution;
	protected boolean printQueryProcStats;
	protected boolean printFedAccessStats;
	protected boolean printOnelineTimeStats;
	protected String queryProcStatsFile;
	protected String fedAccessStatsFile;
	protected String onelineTimeStatsFile;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		cmdLine.getUsage().startCategory("Execution Statistics");
		cmdLine.add( argSuppressResultPrintout, "--suppressResultPrintout", "Do not print out the query result" );
		cmdLine.add( argSkipExecution, "--skipExecution", "Do not execute the query (but create the execution plan)" );
		cmdLine.add( argQueryProcStats, "--printQueryProcStats", "Print out statistics about the query execution process" );
		cmdLine.add( argQueryProcStatsToFile, "--printQueryProcStatsToFile", "Print out statistics about the query execution process to a file" );
		cmdLine.add( argOnelineTimeStats, "--printQueryProcMeasurements",
			"Print out measurements about the query processing time in one line that can be used for a CSV file" );
		cmdLine.add( argOnelineTimeStatsToFile, "--printQueryProcMeasurementsToFile", "Print out measurements about the query processing time to a file" );
		cmdLine.add( argFedAccessStats, "--printFedAccessStats", "Print out statistics of the federation access manager" );
		cmdLine.add( argFedAccessStatsToFile, "--printFedAccessStatsToFile", "Print out statistics of the federation access manager to a file" );
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		suppressResultPrintout = cmdLine.contains(argSuppressResultPrintout);

		skipExecution = cmdLine.contains(argSkipExecution);

		printQueryProcStats = cmdLine.contains(argQueryProcStats);

		printOnelineTimeStats = cmdLine.contains(argOnelineTimeStats);

		printFedAccessStats = cmdLine.contains(argFedAccessStats);

		if ( cmdLine.contains(argQueryProcStatsToFile) )
			queryProcStatsFile = cmdLine.getValue(argQueryProcStatsToFile);

		if ( cmdLine.contains(argOnelineTimeStatsToFile) )
			onelineTimeStatsFile = cmdLine.getValue(argOnelineTimeStatsToFile);

		if ( cmdLine.contains(argFedAccessStatsToFile) )
			fedAccessStatsFile = cmdLine.getValue(argFedAccessStatsToFile);
	}

	public boolean isSuppressResultPrintout() {
		return suppressResultPrintout;
	}

	public boolean isSkipExecution() {
		return skipExecution;
	}

	public boolean isPrintQueryProcStats() {
		return printQueryProcStats;
	}

	public boolean isPrintOnelineTimeStats() {
		return printOnelineTimeStats;
	}

	public boolean isPrintFedAccessStats() {
		return printFedAccessStats;
	}

	public String getQueryProcStatsFile() {
		return queryProcStatsFile;
	}

	public String getOnelineTimeStatsFile() {
		return onelineTimeStatsFile;
	}

	public String getFedAccessStatsFile() {
		return fedAccessStatsFile;
	}

	public boolean needsQueryProcStats() {
		return printQueryProcStats
			|| printOnelineTimeStats
			|| queryProcStatsFile != null
			|| onelineTimeStatsFile != null;
	}

	public boolean needsFedAccessStats() {
		return printFedAccessStats
			|| fedAccessStatsFile != null;
	}

	/**
	 * Executes the given write action using a print stream that appends to
	 * the specified file.
	 *
	 * If the file cannot be opened or the output destination is invalid,
	 * the given error handler is invoked with an appropriate error message.
	 *
	 * @param outputDest the file to write to
	 * @param action the write operation to execute
	 * @param errorHandler handler for reporting errors
	 */
	public static void writeContentToFile( final String outputDest,
	                                       final Consumer<PrintStream> action,
	                                       final Consumer<String> errorHandler ) {
		if ( ! outputDestIsValid(outputDest) ) {
			errorHandler.accept(
				"Invalid output destination: " + outputDest
				+ ". Output destination should be a file path, not an argument."
			);
			return;
		}

		try ( final PrintStream ps =
				new PrintStream(new FileOutputStream(outputDest, true)) ) {
			action.accept(ps);
		}
		catch ( final FileNotFoundException ex ) {
			errorHandler.accept(
				"Failed to create print stream for output destination: "
				+ outputDest
			);
		}
	}

	/**
	 * Writes the given statistics to the specified file.
	 *
	 * @param file destination file
	 * @param stats statistics to write
	 * @param errorHandler handler for reporting errors
	 */
	public static void writeStatsToFile( final String file,
	                                     final Stats stats,
	                                     final Consumer<String> errorHandler ) {
		writeContentToFile(
			file,
			ps -> StatsPrinter.print(stats, ps, true),
			errorHandler
		);
	}

	/**
	 * Validates the output destination for statistics by checking if it starts with a hyphen.
	 * If the output destination is invalid, the method returns false. Otherwise, it returns true.
	 *
	 * @param outputDest the output destination to validate
	 * @return true if the output destination is valid, false otherwise
	 */
	private static boolean outputDestIsValid( final String outputDest ) {
		return ! outputDest.startsWith( "-" );
	}
}
