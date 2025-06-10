package se.liu.ida.hefquin.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Ignore;
import org.junit.Test;

/**
 * Note: The tests in this file that do not analyze actual query results (e.g.,
 * those checking for printed plans, stats, or timings) rely on simplified
 * assertions such as `contains(...)` or regex-based line matches. This is an
 * intentional simplification to avoid dependencies on formatting details. These
 * tests focus on verifying that the CLI reaches and executes the expected code
 * paths, rather than asserting full result correctness.
 */

@Ignore("Disabled since it uses live web tests")
public class RunQueryWithoutSrcSelTest
{
	private static final String queryFile = "TestQuery.rq";
	private static final String fedCatFile = "TestFedCat.ttl";
	private static final String confDescrFile = "TestEngineDescr.ttl";

	@Test
	public void runWithValidArgs() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile
		};
		RunQueryWithoutSrcSel.main(args);

		// Check result
		final String result = baos.toString().replaceAll("\r", "");
		assertEquals( "---------------\n" +
		              "| label       |\n" +
		              "===============\n" +
		              "| \"Berlin\"@en |\n" +
		              "---------------\n", result );
	}

	@Test
	public void runWithFormat() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--results=CSV"
		};
		RunQueryWithoutSrcSel.main(args);

		// Check result
		final String result = baos.toString().replaceAll("\r", "");
		assertEquals("label\nBerlin\n", result);
	}

	@Test
	public void runWissingFedCat() {
		// Run CLI (using mainRun)
		final String[] args = new String[] { "--query=" + queryFile };
		final int exitCode = new RunQueryWithoutSrcSel(args).mainRun(true, false);
		assertEquals(2, exitCode);
	}

	@Test
	public void runWithConfDescr() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--confDescr=" + confDescrFile
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString().replace("\r", "");
		assertEquals( "---------------\n" +
		              "| label       |\n" +
		              "===============\n" +
		              "| \"Berlin\"@en |\n" +
		              "---------------\n", result );
	}

	@Test
	public void runWithSuppressResultPrintout() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--suppressResultPrintout"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		assertEquals("", result);
	}

	@Test
	public void runWithSkipExecution() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--skipExecution"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result (should be empty)
		final String result = baos.toString();
		assertEquals( "---------\n" +
		              "| label |\n" +
		              "=========\n" +
		              "---------\n", result);
	}

	@Test
	public void runWithPrintQueryProcStats() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--printQueryProcStats"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		assertTrue( result.contains("queryPlanningStats") );
	}

	@Test
	public void runWithPrintQueryProcMeasurements() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--suppressResultPrintout",
			"--printQueryProcMeasurements"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		// Match 4 comma-separated integers
		boolean found = result.lines()
			.anyMatch(line -> line.matches("^(\\d+,\\s*){3}\\d+$"));
		assertTrue(found);
	}

	@Test
	public void runWithPrintFedAccessStats() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--suppressResultPrintout",
			"--printFedAccessStats"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		assertTrue( result.contains("numberOfSPARQLRequestsIssued") );
	}

	@Test
	public void runWithPrintSourceAssignment() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--suppressResultPrintout",
			"--printSourceAssignment"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		assertTrue( result.contains("--------- Source Assignment ---------") );
	}

	@Test
	public void runWithPrintLogicalPlan() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--suppressResultPrintout",
			"--printLogicalPlan"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		System.err.println(result);
		assertTrue( result.contains("--------- Logical Plan ---------") );
	}

	@Test
	public void runWithPrintPhysicalPlan() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {
			"--query=" + queryFile,
			"--federationDescription=" + fedCatFile,
			"--suppressResultPrintout",
			"--printPhysicalPlan"
		};
		RunQueryWithoutSrcSel.main(args);
		// Check result
		final String result = baos.toString();
		assertTrue( result.contains("--------- Physical Plan ---------") );
	}

	@Test
	public void invalidArg() {
		// Run CLI (using mainRun)
		final String[] args = new String[] { "--invalid" };
		final int exitCode = new RunQueryWithoutSrcSel( args ).mainRun(true, false);
		assertEquals(1, exitCode);
	}
}
