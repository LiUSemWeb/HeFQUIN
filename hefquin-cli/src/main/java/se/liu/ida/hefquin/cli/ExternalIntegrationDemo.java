package se.liu.ida.hefquin.cli;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.resultset.ResultsFormat;
import org.apache.jena.sparql.util.QueryExecUtils;

import arq.cmdline.CmdARQ;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.QueryProcessingOutput;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;

/**
 * The purpose of this class is to demonstrate how the HeFQUIN engine can be
 * used directly within the Java code of other Java projects. The relevant
 * functions to look at are {@link #execWithHeFQUIN1(String, Query)} and
 * {@link #execWithHeFQUIN2(String, Query)}.
 */
public class ExternalIntegrationDemo extends CmdARQ
{
	protected final ArgDecl argQuery     = new ArgDecl( ArgDecl.HasValue, "query" );
	protected final ArgDecl argFedDescr  = new ArgDecl( ArgDecl.HasValue, "federationDescription" );

	public static void main( final String[] argv ) {
		new ExternalIntegrationDemo( argv ).mainRun();
	}

	public ExternalIntegrationDemo( final String[] argv ) {
		super( argv );

		add( argQuery, "--query",
		     "File containing a SPARQL SELECT query" );

		add( argFedDescr, "--federationDescription",
		     "File with an RDF description of the federation" );
	}

	@Override
	protected String getSummary() {
		return getCommandName() + " --query=<query> --federationDescription=<federation description>";
	}

	@Override
	protected void exec() {
		if ( ! contains(argFedDescr) )
			// Throws an exception that terminates the program.
			cmdError("No federation description file", true);

		if ( ! contains(argQuery) )
			// Throws an exception that terminates the program.
			cmdError("No query file", true);

		final String fedDescrFile = getValue(argFedDescr);
		final Query query = QueryFactory.read( getValue(argQuery) );

		//execWithHeFQUIN1(fedDescrFile, query);
		execWithHeFQUIN2(fedDescrFile, query);
	}

	/**
	 * This function demonstrates how to use the HeFQUIN engine if you simply
	 * want the result of each query printed to some output stream, including
	 * stdout.
	 */
	protected void execWithHeFQUIN1( final String fedDescrFile, final Query query ) {
		// Create a HeFQUINEngine object.
		final HeFQUINEngine hefquin = new HeFQUINEngineBuilder()
				.withFederationCatalog(fedDescrFile)
				.build();

		// Use the HeFQUINEngine object to execute the given query and
		// print the query result to stdout (there are other variations
		// of the function used here, with more parameters, and it is
		// possible to use the HeFQUINEngine object multiple times, for
		// multiple queries).
		QueryProcessingStatsAndExceptions statsAndExcs = null;
		try {
			statsAndExcs = hefquin.executeQueryAndPrintResult(query);
		}
		catch ( final IllegalQueryException ex ) {
			System.err.println( "The given query is invalid: " + ex.getMessage() );
		}
		catch ( final UnsupportedQueryException ex ) {
			System.err.println( "The given query is not supported by HeFQUIN: " + ex.getMessage() );
		}

		// Check whether exceptions occurred during the query execution.
		if ( statsAndExcs != null && statsAndExcs.containsExceptions() ) {
			final int numbOfExcs = statsAndExcs.getExceptions().size();
			System.err.println( numbOfExcs + " exceptions occurred during query execution" );
		}

		// Once the HeFQUINEngine object is not needed anymore, shut it down.
		hefquin.shutdown();
	}

	/**
	 * This function demonstrates how to use the HeFQUIN engine if you want
	 * to process the query result with your program. The implementation here
	 * simply prints the result to stdout, but you can also use it in any
	 * other way.
	 */
	protected void execWithHeFQUIN2( final String fedDescrFile, final Query query ) {
		// Create a HeFQUINEngine object.
		final HeFQUINEngine hefquin = new HeFQUINEngineBuilder()
				.withFederationCatalog(fedDescrFile)
				.build();

		// Use the HeFQUINEngine object to execute the given query and
		// produce an output object that provides access to to the query
		// result (it is possible to call this function multiple times,
		// for multiple queries).
		QueryProcessingOutput qProcOutput = null;
		try {
			qProcOutput = hefquin.executeSelectQuery(query);
		}
		catch ( final IllegalQueryException ex ) {
			System.err.println( "The given query is invalid: " + ex.getMessage() );
		}
		catch ( final UnsupportedQueryException ex ) {
			System.err.println( "The given query is not supported by HeFQUIN: " + ex.getMessage() );
		}

		// Obtain the query result, in the form of a Jena ResultSet object.
		final ResultSet rs = qProcOutput.getResultSet();

		// While the ResultSet can be used in any way you like, here we
		// simply print it to stdout.
		QueryExecUtils.outputResultSet(rs, query.getPrologue(), ResultsFormat.FMT_TEXT, System.out);

		// Check whether exceptions occurred during the query execution.
		// Note that this can only be done after the ResultSet has been
		// consumed.
		final QueryProcessingStatsAndExceptions statsAndExcs = qProcOutput.getStatsAndExceptions();
		if ( statsAndExcs != null && statsAndExcs.containsExceptions() ) {
			final int numbOfExcs = statsAndExcs.getExceptions().size();
			System.err.println( numbOfExcs + " exceptions occurred during query execution" );
		}

		// Once the HeFQUINEngine object is not needed anymore, shut it down.
		hefquin.shutdown();
	}

}
