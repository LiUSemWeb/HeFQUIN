package se.liu.ida.hefquin.fedbench;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.sparql.resultset.ResultsFormat;
import se.liu.ida.hefquin.engine.HeFQUINEngine;
import se.liu.ida.hefquin.engine.HeFQUINEngineBuilder;
import se.liu.ida.hefquin.engine.QueryProcessingStatsAndExceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Base class for FedBench tests.
 */

public abstract class FedbenchTestBase
{
	private static HeFQUINEngine engine;
	public static double DEFAULT_TOLERANCE = 0.5;
	public static long DEFAULT_SLACK = 100;
	private static final Logger LOGGER = LoggerFactory.getLogger(FedbenchTestBase.class);

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
	 * @throws Exception 
	 */
	public static String _execute( final String queryString )
			throws Exception {
		final Query query = QueryFactory.create(queryString);
		final ResultsFormat resultsFormat = ResultsFormat.FMT_RS_JSON;
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final QueryProcessingStatsAndExceptions statsAndExceptions;
		try ( PrintStream ps = new PrintStream( baos, true, StandardCharsets.UTF_8 ) ) {
			statsAndExceptions = engine.executeQueryAndPrintResult( query, resultsFormat, ps );
		}

		if ( statsAndExceptions != null ) {
			if ( statsAndExceptions.getExceptions() != null ) {
				final List<String> exceptions = new ArrayList<>();
				statsAndExceptions.getExceptions().forEach( c -> {
					exceptions.add(c.getLocalizedMessage());
					exceptions.add(c.getCause().toString());
					c.printStackTrace();
				} );
				throw new Exception( "Query failed with exceptions:\n" + String.join( "\n", exceptions ));
			}
		}

		return baos.toString();
	}

	/**
	 * Loads a SPARQL query from {@code queryFile}, executes it, and validates the
	 * actual result set against an expected result loaded from {@code resultFile}.
	 *
	 * This method is the main entry point for FedBench test cases.
	 *
	 * @param queryFile  path to the SPARQL query file
	 * @param resultFile path to the expected result file
	 * @throws Exception if reading resources fails or query execution fails
	 */
	public void _executeQuery( final String queryFile, final String resultFile )
			throws Exception {
		String query;
		try ( final InputStream is = getClass().getClassLoader().getResourceAsStream(queryFile) ) {
			query = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
		}

		final long t0 = System.currentTimeMillis();
		final String result = _execute(query);
		final long t1 = System.currentTimeMillis();

		String expected;
		try( final InputStream is = getClass().getClassLoader().getResourceAsStream(resultFile) ) {
			expected = new String( is.readAllBytes(), StandardCharsets.UTF_8 );
		}

		// Validate results
		assertTrue(
			String.format("Mismatch between expected and actual result\n" +
			              "Expected:\n%s\n" +
			              "Actual:\n%s\n", expected, result),
			resultSetEqual(expected, result)
		);
	
		// Log execution time
		LOGGER.debug("Query executed in {} ms", (t1 - t0));
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
			final Set<String> s1 = resultSetToStringSet(rs1);
			final Set<String> s2 = resultSetToStringSet(rs2);
			LOGGER.debug("Expected size {} vs actual size {}", s1.size(), s2.size() );

			return s1.equals(s2);
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
