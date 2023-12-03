package se.liu.ida.hefquin.cli;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModLangOutput;
import arq.cmdline.ModTime;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.sparql.core.Var;
import se.liu.ida.hefquin.cli.modules.ModLPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.Record2SolutionMappingTranslator;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.Record2SolutionMappingTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MaterializeRDFViewOfLPG extends CmdARQ
{
	protected final ModTime modTime =            new ModTime();
	protected final ModLangOutput ModLangOut =   new ModLangOutput();
	protected final ModLPG2RDFConfiguration modLPG2RDFConfiguration = new ModLPG2RDFConfiguration();

	protected final ArgDecl argEndpointURI   = new ArgDecl(ArgDecl.HasValue, "endpoint");


	protected final String query = "";


	public static void main( final String[] args ) {
		new MaterializeRDFViewOfLPG(args).mainRun();
	}

	protected MaterializeRDFViewOfLPG(final String[] argv ) {
		super(argv);

		addModule(modTime);
		addModule(ModLangOut);

		addModule(modLPG2RDFConfiguration);

		add(argEndpointURI, "--endpoint", "The URI of the Neo4j endpoint");
	}

	@Override
	protected String getSummary() {
		return getCommandName() + "--endpoint=<Neo4j endpoint URI> --time?";
	}

	@Override
	protected void exec() {
		final LPG2RDFConfiguration conf = modLPG2RDFConfiguration.getLPG2RDFConfiguration();
		final RecordsResponse response = performQueryExecution();

		//TODO: Do Translation
		final List<SolutionMapping> result = new ArrayList<>(); //performResultTranslation( response, conf, new CypherUnionQueryImpl(), this.cypherVarVarMap);

		serializeOutput(result);
	}

	protected RecordsResponse performQueryExecution() {
		final Neo4jRequest request = new Neo4jRequestImpl( this.query.toString() );

		final String uri = getArg(argEndpointURI).getValue();
		final Neo4jInterface iface = new Neo4jInterfaceImpl(uri);
		final Neo4jServer server = new Neo4jServer() {
			@Override public Neo4jInterface getInterface() { return iface; }

			@Override public VocabularyMapping getVocabularyMapping() { return null; }
		};

		final Neo4jRequestProcessor processor = new Neo4jRequestProcessorImpl();

		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final RecordsResponse response;
		try {
			response = processor.performRequest(request, server);
		} catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( ex.getMessage() );
			ex.printStackTrace( System.err );
			throw new IllegalStateException(ex);
		}

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Query Execution Time: " + modTime.timeStr(time) + " sec");
		}

		return response;
	}

	protected List<SolutionMapping> performResultTranslation( final RecordsResponse response,
															  final LPG2RDFConfiguration conf,
															  final CypherQuery query,
															  final Map<CypherVar,Var> varMap ) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}
		final Record2SolutionMappingTranslator translator = new Record2SolutionMappingTranslatorImpl();
		final List<SolutionMapping> result = translator.translateRecords( response.getResponse(),
				conf,
				query,
				varMap );

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
		}

		return result;
	}

	protected void serializeOutput( final List<SolutionMapping> result ) {
		//TODO: Serialize Data
		System.out.println("RESULTS");
	}

}
