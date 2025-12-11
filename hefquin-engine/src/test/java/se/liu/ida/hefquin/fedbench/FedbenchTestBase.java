package se.liu.ida.hefquin.fedbench;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;

import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.IllegalQueryException;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;
import se.liu.ida.hefquin.engine.UnsupportedQueryException;

/**
 * Base class for FedBench tests.
 */

public abstract class FedbenchTestBase
{
	private static HeFQUINEngine engine;

	public static void init( final String fedCat ) {
		engine = new HeFQUINEngineBuilder()
			.withFederationCatalog(fedCat)
			.withEngineConfiguration("config/DefaultConfDescr.ttl")
			.build();
	}

	/**
	 * Executes a SPARQL query and returns the result set encoded as SPARQL Results JSON.
	 *
	 * @param queryString the SPARQL query to execute
	 * @return the query result encoded as a SPARQL JSON result string
	 * @throws UnsupportedQueryException
	 * @throws IllegalQueryException
	 */
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
	public static void assertWithinTimeBudget( long baseline, long actual ) {
		assertWithinTimeBudget( baseline, actual, 0.5, 500 );
	}

	/**
	 * Verifies that a measured execution time does not represent a performance
	 * regression.
	 * 
	 * @param baseline  average time from previous measurements (ms)
	 * @param actual    time from current run (ms)
	 * @param tolerance relative tolerance compared with baseline
	 * @param slack     absolute slack in ms added on top (ms)
	 */
	public static void assertWithinTimeBudget( long baseline,
	                                           long actual,
	                                           double tolerance,
		                                       long slack ) {
		double allowedMax = baseline * (1.0 + tolerance) + slack;
		if ( actual > allowedMax ) {
			final String msg = String.format(
				"Query too slow: actual=%.2fms, baseline=%.2fms, allowedMax=%.2fms",
				actual,
				baseline,
				allowedMax );
			throw new AssertionError(msg);
		}
	}

	/**
	 * Loads a benchmark SPARQL query and formats it with the given parameter values
	 * (if any). The query is then executed and the result validated against the
	 * expected result, and checks that the execution time is within the allowed
	 * performance budget.
	 *
	 * This method is the main entry point for FedBench test cases.
	 *
	 * @param f        base filename (without extension) for the query and results
	 * @param baseline baseline execution time in milliseconds
	 * @param values   optional formatting parameters applied to the query string
	 * @throws IOException
	 * @throws IllegalQueryException
	 * @throws UnsupportedQueryException
	 *
	 * @throws Exception
	 */
	public void _executeQuery( final String f, final long baseline, final Object[] values )
			throws IOException, UnsupportedQueryException, IllegalQueryException {
		String query;
		try( final InputStream is = getClass().getClassLoader().getResourceAsStream(f + ".rq") ) {
			query = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
			query = String.format(query, values);
		}

		final long t0 = System.currentTimeMillis();
		final String result = _execute(query);
		final long t1 = System.currentTimeMillis();

		String expected;
		try( final InputStream is = getClass().getClassLoader().getResourceAsStream(f + "_results.json") ) {
			expected = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
		}

		// Validate results
		assertTrue(resultSetEqual(expected, result));
		// Assert execution time within range
		assertWithinTimeBudget(baseline, t1-t0);
	}

	/**
	 * Compares two SPARQL result sets for equality by converting each row
	 * (QuerySolution) into a canonical string representation and comparing the
	 * resulting sets of strings.
	 *
	 * @param expected JSON-encoded expected SPARQL result set
	 * @param actual   JSON-encoded actual SPARQL result set
	 * @return true if the result sets contain exactly the same rows, false
	 *         otherwise
	 * @throws IOException
	 */
	public static boolean resultSetEqual( final String expected, String actual )
			throws IOException {
		try( final InputStream is1 = new ByteArrayInputStream(expected.getBytes(StandardCharsets.UTF_8));
		     final InputStream is2 = new ByteArrayInputStream(actual.getBytes(StandardCharsets.UTF_8) )) {
			final ResultSet rs1 = ResultSetFactory.fromJSON(is1);
			final ResultSet rs2 = ResultSetFactory.fromJSON(is2);
			return resultSetToStringSet(rs1).equals(resultSetToStringSet(rs2));
		}
	}

	/**
	 * Converts a Jena ResultSet into a set of canonical string representations.
	 *
	 * @param rs the result set to convert
	 * @return a set of strings
	 */
	private static Set<String> resultSetToStringSet( final ResultSet rs ) {
		final Set<String> result = new HashSet<>();
		while (rs.hasNext()) {
			result.add(rs.next().toString());
		}
		return result;
	}
}
