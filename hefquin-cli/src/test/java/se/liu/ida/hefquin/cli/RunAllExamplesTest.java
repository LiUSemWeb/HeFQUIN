package se.liu.ida.hefquin.cli;

import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Integration test that verifies all runnable example queries can be executed
 * successfully through the HeFQUIN CLI.
 *
 * <p>
 * The test copies the repository's {@code examples/} directory into the CLI
 * module so that example files referenced by relative paths in the federation
 * description are available during execution. Each {@code .rq} query is then
 * executed using the example federation description and default engine
 * configuration. The test fails if execution produces output on {@code stderr}.
 * </p>
 */

@Ignore("Disabled since it uses live web tests")
public class RunAllExamplesTest
{
	final String confDescr = "config/DefaultConfDescr.ttl";
	// final String confDescr = "config/DefaultConfDescrExtended.ttl";
	private Path examplesDir;
	private Path confDescrPath;

	@Before
	public void setUp() throws IOException {
		final Path projectRoot = Paths.get("").toAbsolutePath().getParent();
		confDescrPath = projectRoot.resolve(confDescr);
		
		final Path sourceExamples = projectRoot.resolve("examples");
		examplesDir = Paths.get("examples").toAbsolutePath();
		copyDirectory(sourceExamples, examplesDir);
	}

	@After
	public void tearDown() throws IOException {
		deleteDirectory(examplesDir);
	}
	
	@Test
	public void allExampleQueriesRun() throws IOException, InterruptedException {
		final String fedCatFile = examplesDir
			.resolve("ExampleFederation.ttl")
			.toAbsolutePath()
			.toString();

		final List<String> queries = new ArrayList<>();

		for ( final File file : examplesDir.toFile().listFiles() ) {
			if ( file.isFile() && file.getName().endsWith(".rq") ) {
				queries.add( file.toPath().toAbsolutePath().toString() );
			}
		}
		final PrintStream out = System.out;
		final PrintStream err = System.err;
		final ByteArrayOutputStream stdout = new ByteArrayOutputStream();
		final ByteArrayOutputStream stderr = new ByteArrayOutputStream();

		System.setOut( new PrintStream(stdout) );
		System.setErr( new PrintStream(stderr) );

		try {
			for( final String query : queries ) {
				stdout.reset();
				stderr.reset();

				// Run CLI
				final String[] args = new String[] {
					"--query=" + query,
					"--confDescr=" + confDescrPath,
					"--federationDescription=" + fedCatFile
				};
				new RunQueryWithoutSrcSel(args).mainRun(false, false);

				final String result = stdout.toString();
				final String error = stderr.toString();
				
				out.println(result);
				err.println(error);
				assertTrue(query + ": " + error, error.isEmpty() );
			}
		}
		finally {
			System.setOut(out);
			System.setErr(err);
		}
	}

	private static void copyDirectory( final Path source, final Path target ) throws IOException {
		Files.createDirectories( target );

		final File[] files = source.toFile().listFiles();
		if ( files == null ) {
			throw new IOException( "Could not list directory: " + source );
		}

		for ( final File file : files ) {
			final Path targetPath = target.resolve( file.getName() );

			if ( file.isDirectory() ) {
				copyDirectory( file.toPath(), targetPath );
			}
			else {
				Files.copy( file.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING );
			}
		}
	}

	private static void deleteDirectory( final Path directory ) throws IOException {
		if ( ! Files.exists(directory) ) {
			return;
		}

		final File[] files = directory.toFile().listFiles();
		if ( files != null ) {
			for ( final File file : files ) {
				final Path path = file.toPath();

				if ( file.isDirectory() ) {
					deleteDirectory(path);
				}
				else {
					Files.delete(path);
				}
			}
		}

		Files.delete(directory);
	}
}
