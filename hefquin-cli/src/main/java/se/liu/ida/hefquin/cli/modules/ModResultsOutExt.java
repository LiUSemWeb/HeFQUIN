package se.liu.ida.hefquin.cli.modules;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdArgModule;
import org.apache.jena.cmd.CmdGeneral;

import arq.cmdline.ModResultsOut;

/**
 * Command-line argument module for specifying result output,
 * query processing statistics and federation access statistics.
 */
public class ModResultsOutExt extends ModResultsOut
{
	protected final ArgDecl argOutputToFile           = new ArgDecl( ArgDecl.HasValue, "outputToFile" );
	protected final ArgDecl argSuppressResultPrintout = new ArgDecl( ArgDecl.NoValue, "suppressResultPrintout" );
	protected final ArgDecl argSkipExecution          = new ArgDecl( ArgDecl.NoValue, "skipExecution" );

	protected boolean suppressResultPrintout;
	protected boolean skipExecution;
	protected String outputFile;

	@Override
	public void registerWith( final CmdGeneral cmdLine ) {
		// -------------
		// Instead of using the super-class functionality ...
		//super.registerWith(cmdLine);
		// ... we do that part also ourselves:
		cmdLine.getUsage().startCategory("Results");
		cmdLine.add( resultsFmtDecl,
		             "--rfmt",
		             "Result format (Result set: text, XML, JSON, CSV, TSV; Graph: TTL, NTRIPLES, JSONLD, RDF/XML)") ;
		// -------------

		cmdLine.add( argOutputToFile, "--outputToFile", "Output file (optional, printing to stdout if omitted)" );
		cmdLine.add( argSuppressResultPrintout, "--suppressResultPrintout", "Do not print out the query result" );
		cmdLine.add( argSkipExecution, "--skipExecution", "Do not execute the query (but create the execution plan)" );
	}

	@Override
	public void processArgs( final CmdArgModule cmdLine ) {
		super.processArgs(cmdLine);

		suppressResultPrintout = cmdLine.contains(argSuppressResultPrintout);

		skipExecution = cmdLine.contains(argSkipExecution);
	}

	public String getOutputFile() {
		return outputFile;
	}

	public boolean isSuppressResultPrintout() {
		return suppressResultPrintout;
	}

	public boolean isSkipExecution() {
		return skipExecution;
	}

}
