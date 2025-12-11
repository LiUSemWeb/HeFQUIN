package se.liu.ida.hefquin.fedbench;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;

public abstract class FedbenchTestBase
{
	private static HeFQUINEngine engine;

	public static void init( final String fedCat ) {
		engine = new HeFQUINEngineBuilder()
				.withFederationCatalog(fedCat)
				.withEngineConfiguration("config/DefaultConfDescr.ttl")
				.build();
	}

	public static String _execute( final String queryString )
			throws UnsupportedQueryException, IllegalQueryException {
		final Query query = QueryFactory.create(queryString);
		final ResultsFormat resultsFormat = ResultsFormat.FMT_RS_JSON;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final QueryProcessingStatsAndExceptions statsAndExceptions;
		try ( PrintStream ps = new PrintStream( baos, true, StandardCharsets.UTF_8 ) ) {
			statsAndExceptions = engine.executeQueryAndPrintResult( query, resultsFormat, ps );
		}

		if ( statsAndExceptions != null ) {
			if ( statsAndExceptions.getExceptions() != null ) {
				statsAndExceptions.getExceptions().forEach( c -> {
					System.err.println( c.getCause() );
					System.err.println( c.getLocalizedMessage() );
				} );
			}
		}

		return baos.toString();
	}

	/**
	 * Verifies that a measured execution time does not represent a performance
	 * regression. Uses default values for tolerance (50%) and slack (500ms).
	 * 
	 * @param baseline  average time from previous measurements (ms)
	 * @param actual    time from current run (ms)
	 */
	public static void assertWithinTimeBudget( double baseline, double actual ) {
		assertWithinTimeBudget( baseline, actual, 0.5, 500 );
	}

	/**
	 * Verifies that a measured execution time does not represent a performance
	 * regression.
	 * 
	 * @param baseline  average time from previous measurements (ms)
	 * @param actual    time from current run (ms)
	 * @param tolerance relative tolerance compared with baseline
	 * @param slackMs   absolute slack in ms added on top (ms)
	 */
	public static void assertWithinTimeBudget( double baseline, double actual, double tolerance, double slack ) {
		double allowedMax = baseline * (1.0 + tolerance) + slack;
		if ( actual > allowedMax ) {
			final String msg = String.format( "Query too slow: actual=%.2fms, baseline=%.2fms, allowedMax=%.2fms",
			                                  actual,
			                                  baseline,
			                                  allowedMax );
			throw new AssertionError(msg);
					
		}
	}
}
