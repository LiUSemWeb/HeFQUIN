package se.liu.ida.hefquin.cli;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.nio.file.Files;

import org.junit.Test;

public class MaterializeRDFViewFromRMLTest
{
	private static final String mappingFile = "src/test/resources/ExampleMappingTest.ttl";

	@Test
	public void runWithValidArgs() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setOut( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] { "--mapping=" + mappingFile };
		final int exitCode = new MaterializeRDFViewFromRML(args).mainRun(false, false);
		assertEquals( 0, exitCode );

		// Check result
		final String result = baos.toString().replaceAll("\r", "");
		assertEquals( "_:BSweden <http://example.org/totalCases> \"999\"^^<http://www.w3.org/2001/XMLSchema#integer> .\n", result );
	}

	@Test
	public void runWithMissingMapping() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] {};
		final int exitCode = new MaterializeRDFViewFromRML(args).mainRun(true, false);
		assertEquals( 2, exitCode );

		// Check result
		final String result = baos.toString();
		assertTrue( result.startsWith("Must give an RDF input file") );
	}

	@Test
	public void outputToFile_createsExpectedResult() throws Exception {
		final File out = File.createTempFile( "hefquin", ".nt" );
		out.deleteOnExit();

		final String[] args = {
			"--mapping=" + mappingFile,
			"--outputToFile=" + out.getAbsolutePath()
		};

		final int exitCode = new MaterializeRDFViewFromRML(args).mainRun(false, false);
		assertEquals( 0, exitCode );

		final String result = Files.readString( out.toPath() );
		assertTrue( result.contains("http://example.org/totalCases") );
	}

	@Test
	public void invalidArg() {
		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		System.setErr( new PrintStream(baos) );

		// Run CLI
		final String[] args = new String[] { "--invalid" };
		final int exitCode = new MaterializeRDFViewFromRML(args).mainRun(false, false);
		assertEquals(1, exitCode);

		// Check result
		final String result = baos.toString();
		assertTrue( result.startsWith("Unknown argument: invalid") );
	}
}
