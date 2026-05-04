package se.liu.ida.hefquin.cli;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ResIterator;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.vocabulary.RDF;

import com.jayway.jsonpath.JsonPathException;

import arq.cmdline.CmdARQ;
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
public class MaterializeRDFViewFromRML extends CmdARQ
{
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

		add( argRdfFile, "--mapping", "RML mapping file" );
		add( argOutputToFile, "--outputToFile", "Output file (optional, printing to stdout if omitted)" );
		add( argBaseIRI, "--baseIRI", "Base IRI for mapping (optional, hardcoded IRI used if omitted)" );
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
			"--mapping=<rdf-file> " +
			"[--outputToFile=<file-name>] " +
			"[--baseIRI=<iri>]";
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
		if ( ! contains(argRdfFile) )
			cmdError("Must give an RDF input file", true );

		final Model rdfModel = RDFDataMgr.loadModel( getValue( argRdfFile ) );

		final Node chosenBaseIRI =
			contains(argBaseIRI) ? NodeFactory.createURI( getValue( argBaseIRI ) ) :
		                           NodeFactory.createURI( "http://example.org/FixedBaseIRI/HardcodedInMaterializeRDFViewFromRML/" );

		OutputStream outputStream = System.out;
		if ( contains(argOutputToFile) ) {
			try {
				// Appends to file rather than overwriting
				outputStream = new FileOutputStream( getValue( argOutputToFile ), true );
			} catch ( final FileNotFoundException e ) {
				cmdError( "Failed to create print stream for output destination: " + getValue( argOutputToFile ), false );
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
		while ( iter.hasNext() ) {
			final Resource tm = iter.next();
			final MappingExpression trMap;
			try {
				trMap = RML2MappingAlgebra.convert( tm,
				                                    rmlDescr,
				                                    baseIRI );
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

		// NOTE: currently using a fixed JSON source for evaluation.
		final String jsonString;
		try {
			jsonString = Files.readString(Path.of("examples/ExampleJSONSource.json"));
		}
		catch ( final Exception e ) {
			cmdError( "Failed to read sources.json: " + e.getMessage(), true );
			return; // Primarily used to avoid "variable not initialized" compiler error
		}

		final JsonObject jsonObject;
		try {
			jsonObject = new JsonObject(jsonString);
		}
		catch ( final JsonPathException e ) {
			cmdError( "Invalid JSON input: failed to parse JSON string.", true );
			return;
		}

		final Map<SourceReference,DataObject> map = new HashMap<>();
		for ( final SourceReference sr : MappingExpressionUtils.extractAllSrcRefs(expr) ) {
			map.put( sr, jsonObject );
		}

		// Measure only the core RML evaluation + materialization phase
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final MappingRelation mappingRelation = MappingExpressionUtils.evaluate( expr, map );

		final Dataset dataset = MappingRelationUtils.convertToRDF(mappingRelation);

		// Write the model to assigned output stream
		RDFDataMgr.write( out, dataset.getDefaultModel(), modLangOut.getOutputStreamFormat() );

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Overall Processing Time: " + modTime.timeStr(time) + " sec");
		}
	}
}
