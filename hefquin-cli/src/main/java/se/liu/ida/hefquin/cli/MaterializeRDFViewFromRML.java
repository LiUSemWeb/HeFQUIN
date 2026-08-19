package se.liu.ida.hefquin.cli;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.cmd.CmdGeneral;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sys.JenaSystem;
import org.apache.jena.vocabulary.RDF;

import com.jayway.jsonpath.JsonPathException;

import arq.cmdline.ModLangOutput;
import arq.cmdline.ModTime;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationUtils;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpression;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpressionFactory;
import se.liu.ida.hefquin.mappings.algebra.exprs.MappingExpressionUtils;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.rml.RML2MappingAlgebra;
import se.liu.ida.hefquin.mappings.rml.RML2MappingAlgebra.FileSourceReference;
import se.liu.ida.hefquin.mappings.rml.RMLParserException;
import se.liu.ida.hefquin.mappings.rml.vocabulary.RMLVocab;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;
import se.liu.ida.hefquin.mappings.sources.json.JsonObject;

/**
 * A command-line tool to materialize an RDF view from an RML mapping.
 * The tool reads an RDF document containing one or more RML Triples Maps,
 * converts them into a mapping algebra expression, evaluates the mapping
 * against provided data sources and materializes the resulting RDF graph.
 * The output RDF can be written in different serialization formats and
 * optionally stored in a file or printed to standard output.
 */
public class MaterializeRDFViewFromRML extends CmdGeneral
{
	static {
		JenaSystem.init();
	}

	protected final ModTime modTime =            new ModTime();
	protected final ModLangOutput modLangOut =   new ModLangOutput();

	protected final ArgDecl argRdfFile =      new ArgDecl( ArgDecl.HasValue, "mapping" );
	protected final ArgDecl argOutputToFile = new ArgDecl( ArgDecl.HasValue, "outputToFile" );
	protected final ArgDecl argBaseIRI =      new ArgDecl( ArgDecl.HasValue, "baseIRI" );

	/**
	 * Main entry point of the tool, accepting command-line arguments to specify the
	 * mapping details and output format options.
	 *
	 * @param args Command-line arguments.
	 */
	public static void main( final String[] args ) {
		new MaterializeRDFViewFromRML( args ).mainRun();
	}

	/**
	 * Constructor that initializes the command-line tool with necessary argument
	 * modules for output format and timing options and defines command-line options
	 * for the RML mapping file, output destination and base IRI.
	 *
	 * @param argv Command-line arguments.
	 */
	protected MaterializeRDFViewFromRML( final String[] argv ) {
		super(argv);

		addModule(modTime);
		addModule(modLangOut);

		add( argOutputToFile, "--outputToFile=file", "Output file (optional, printing to stdout if omitted)" );

		getUsage().startCategory("RML-specific arguments");
		add( argRdfFile, "--mapping=file", "RML mapping file" );
		add( argBaseIRI, "--baseIRI=IRI", "Base IRI for the mapping process (optional, hardcoded IRI used if omitted)" );
	}

	@Override
	protected String getCommandName() {
		return "hefquin-rmlmat";
	}

	/**
	 * Returns the usage summary string of the command, showing the required and
	 * optional arguments.
	 *
	 * @return A string that describes the usage of the command.
	 */
	@Override
	protected String getSummary() {
		return "Usage: " + getCommandName() + " " +
			"--mapping <file> " +
			"[--outputToFile <file>] " +
			"[--baseIRI <iri>]";
	}

	/**
	 * Parses command-line arguments and prepares execution parameters for the RML materialization process.
	 *
	 * <p>
	 * This includes loading the RML mapping file into a Jena Model, determining the base IRI
	 * and configuring the output destination. The actual materialization logic is delegated
	 * to {@link #exec(Model, Node, OutputStream)}.
	 * </p>
	 */
	@Override
	protected void exec() {
		try {
			validateMappingArg();
		}
		catch ( final IllegalArgumentException ex ) {
			cmdError( ex.getMessage(), true );
		}

		final Model rdfModel = RDFDataMgr.loadModel( getValue(argRdfFile) );

		// If no base IRI is given, the default one must be: http://example.org/
		// ( see the last sentence of Sec.4.1.1 of the RML-Core spec:
		//   https://kg-construct.github.io/rml-core/spec/docs/#base-iri )
		final Node chosenBaseIRI =
			contains(argBaseIRI) ? NodeFactory.createURI( getValue(argBaseIRI) ) :
		                           NodeFactory.createURI( "http://example.org/" );

		OutputStream outputStream = System.out;
		if ( contains(argOutputToFile) ) {
			try {
				// Should not append to the file but overwrite it
				outputStream = new FileOutputStream( getValue(argOutputToFile) );
			} catch ( final FileNotFoundException e ) {
				cmdError( "Failed to create print stream for output destination: " + getValue(argOutputToFile), false );
			}
		}

		exec( rdfModel, chosenBaseIRI, outputStream );
	}

	/**
	 * Executes the RML materialization process.
	 * <p>
	 * The method performs the full pipeline of reading an RDF file containing RML triples maps,
	 * converting each triples map into a mapping expression, combining all expressions into a
	 * single union expression and evaluating the resulting mapping against a fixed JSON data
	 * source.
	 * <p>
	 * The evaluation result is transformed into an RDF dataset, which is then serialized and
	 * written either to standard output or to a user-specified file. Optionally, a base IRI
	 * can be provided for the mapping process; otherwise, a default base IRI is used.
	 * <p>
	 */
	protected void exec( final Model rmlDescr,
	                     final Node baseIRI,
	                     final OutputStream out ) {
		final ResIterator iter = rmlDescr.listResourcesWithProperty( RDF.type, RMLVocab.TriplesMap );
		final List<MappingExpression> trMaps = new ArrayList<>();
		final File rdfFile = (new File( getValue(argRdfFile) )).getAbsoluteFile();
		final File mappingDir = rdfFile.getParentFile();
		while ( iter.hasNext() ) {
			final Resource tm = iter.next();
			final MappingExpression trMap;
			try {
				trMap = RML2MappingAlgebra.convert( tm,
				                                    rmlDescr,
				                                    baseIRI,
				                                    mappingDir );
			}
			catch ( final RMLParserException e ) {
				cmdError("There is a problem in the RML mapping: " +  e.getMessage(), true );
				return;
			}

			trMaps.add(trMap);
		}

		if ( trMaps.isEmpty() ) {
			cmdError( "No rml:TriplesMap found in the input file", true );
		}

		final MappingExpression[] exprs = new MappingExpression[ trMaps.size() ];
		final MappingOperator op = MappingOpProject.createWithSPOG();
		int i = 0;
		for ( final MappingExpression trMapExpr : trMaps ) {
			exprs[i++] = MappingExpressionFactory.create(op, trMapExpr);
		}

		final MappingExpression expr = MappingExpressionFactory.create(
				MappingOpUnion.getInstance(),
				exprs );

		final Map<SourceReference,DataObject> map = new HashMap<>();
		for ( final SourceReference sr : MappingExpressionUtils.extractAllSrcRefs(expr) ) {
			if ( sr instanceof FileSourceReference fsr ) {
				final File file = fsr.getFile();

				final String jsonString;
				try {
					jsonString = Files.readString(file.toPath());
				}
				catch ( final Exception e ) {
					cmdError( "Failed to read " + file.getPath() + ":" + e.getMessage(), true );
					return; // Primarily used to avoid "variable not initialized" compiler error
				}

				final JsonObject jsonObject;
				try {
					jsonObject = new JsonObject(jsonString);
				}
				catch ( final JsonPathException e ) {
					cmdError( "Parsing JSON string from " + file.getPath() + " failed:" + e.getMessage(), true );
					return;
				}

				map.put( sr, jsonObject );
			}
			else {
				cmdError(
					"MaterializeRDFViewFromRML only supports file-based logical sources. "
					+ "Found source reference of type "
					+ sr.getClass().getSimpleName(),
					true
				);
				return;
			}
		}

		// Measure only the core RML evaluation + materialization phase
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final MappingRelation mappingRelation = MappingExpressionUtils.evaluate( expr, map );

		final Dataset dataset = MappingRelationUtils.convertToRDF(mappingRelation);

		final OutputStream outputStream = setupOutputStream(out);
		final RDFFormat outputFormat = getOutputFormat();

		// Write the model to assigned output stream
		RDFDataMgr.write( outputStream, dataset.getDefaultModel(), outputFormat );

		if ( outputStream instanceof GZIPOutputStream gzip ) {
			try {
				gzip.finish();
			}
			catch ( final IOException e ) {
				throw new RuntimeIOException(
					"Finishing the compressed output stream caused an exception.", e );
			}
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.err.println("Overall Processing Time: " + modTime.timeStr(time) + " sec");
		}
	}

	/**
	 * Sets up the output stream for writing RDF data. If output compression is
	 * enabled, the given output stream is wrapped in a GZIP output stream.
	 *
	 * @param outStreamBase the base output stream (e.g., {@code System.out})
	 * @return the output stream to use for writing the RDF data
	 */
	protected OutputStream setupOutputStream( final OutputStream outStreamBase ) {
		if ( modLangOut.compressedOutput() ) {
			try {
				return new GZIPOutputStream(outStreamBase, true);
			}
			catch ( final IOException e ) {
				throw new RuntimeIOException(
					"Setting up the GZIPOutputStream caused an exception.", e );
			}
		}

		return outStreamBase;
	}

	/**
	 * Determines the RDF format to use for output. If a streaming output format
	 * is configured, that format is returned; otherwise, the configured formatted
	 * output format is returned.
	 *
	 * @return the RDF format to use for output
	 */
	protected RDFFormat getOutputFormat() {
		final RDFFormat streamFormat = modLangOut.getOutputStreamFormat();

		if ( streamFormat != null )
			return streamFormat;

		return modLangOut.getOutputFormatted();
	}

	/**
	 * Validates that the required RDF mapping file argument is provided.
	 *
	 * @throws IllegalArgumentException if no RDF input file argument is provided
	 */
	protected void validateMappingArg() {
		if ( ! contains(argRdfFile) ) {
			throw new IllegalArgumentException( "No RML mapping file provided." );
		}
	}
}
