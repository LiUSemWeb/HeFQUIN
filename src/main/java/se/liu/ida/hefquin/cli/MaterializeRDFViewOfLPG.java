package se.liu.ida.hefquin.cli;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModLangOutput;
import arq.cmdline.ModTime;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Prologue;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.util.QueryExecUtils;
import se.liu.ida.hefquin.cli.modules.ModLPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
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
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.PropertyMap;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.impl.Record2SolutionMappingTranslatorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.CypherUnionQueryImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.*;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherExpressionVisitor;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherVarGenerator;

import java.util.*;

public class MaterializeRDFViewOfLPG extends CmdARQ
{
	protected final ModTime modTime =            new ModTime();
	protected final ModLangOutput modLangOut =   new ModLangOutput();
	protected final ModLPG2RDFConfiguration modLPG2RDFConfiguration = new ModLPG2RDFConfiguration();

	protected final ArgDecl argEndpointURI   = new ArgDecl(ArgDecl.HasValue, "endpoint");


	public static void main( final String[] args ) {
		new MaterializeRDFViewOfLPG(args).mainRun();
	}

	protected MaterializeRDFViewOfLPG(final String[] argv ) {
		super(argv);

		addModule(modTime);
		addModule(modLangOut);

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

		final CypherQuery getNodesQuery = buildGetNodesQuery();
		final CypherQuery getEdgesQuery = buildGetEdgesQuery();

		final RecordsResponse nodesResponse = performQueryExecution(getNodesQuery);
		final RecordsResponse edgesResponse = performQueryExecution(getEdgesQuery);

		printNodesOutput(nodesResponse);
		printEdgesOutput(edgesResponse);
	}

	protected RecordsResponse performQueryExecution(final CypherQuery query) {
		final Neo4jRequest request = new Neo4jRequestImpl( query.toString() );

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

	public CypherQuery buildGetNodesQuery(){

//		MATCH (n) RETURN n AS node, 'label' AS label, HEAD(LABELS(n)) AS value
		final CypherVar n = new CypherVar("n");
		return new CypherQueryBuilder()
				.addMatch(new NodeMatchClause(n))
				.addReturn(new AliasedExpression(n, new CypherVar("node")))
				.addReturn(new AliasedExpression(new LiteralExpression("label"), new CypherVar("label")))
				.addReturn(new AliasedExpression(new LabelsExpression(n), new CypherVar("value")))
				.build();
	}

	public CypherQuery buildGetEdgesQuery(){

		final CypherVar n1 = new CypherVar("n1");
		final CypherVar e = new CypherVar("e");
		final CypherVar n2 = new CypherVar("n2");

//		MATCH (n1)-[e]->(n2) RETURN ID(n1) AS nid1, ID(n2) AS nid2, e As edge, TYPE(e) AS rel
		return new CypherQueryBuilder()
				.addMatch(new EdgeMatchClause(n1, e, n2))
				.addReturn(new AliasedExpression(new VariableIDExpression(n1), new CypherVar("nid1")))
				.addReturn(new AliasedExpression(new VariableIDExpression(n2), new CypherVar("nid2")))
				.addReturn(new AliasedExpression(e, new CypherVar("edge")))
				.addReturn(new AliasedExpression(new TypeExpression(e), new CypherVar("rel")))
				.build();
	}

	protected void printNodesOutput( final RecordsResponse nodesResponse) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}
		for (final TableRecord record : nodesResponse.getResponse()) {
			final LPGNodeValue nodeValue = (LPGNodeValue)(record.getEntry(0).getValue());
			System.out.print(nodeValue.getNode().getId());
			System.out.print(",");
			System.out.print((LiteralValue)(record.getEntry(1).getValue()));
			System.out.print(",");
			System.out.print((LiteralValue)(record.getEntry(2).getValue()));
			System.out.println();

			System.out.print(nodeValue.getNode().getId());
			PropertyMap properties = nodeValue.getNode().getProperties();
			for (String name: properties.getPropertyNames()) {
				System.out.print(",");
				System.out.print(name);
				System.out.print(",");
				System.out.print(properties.getValueFor(name));
			}
			System.out.println();
		}
		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
		}
	}

	protected void printEdgesOutput( final RecordsResponse edgesResponse) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}
		for (final TableRecord record : edgesResponse.getResponse()) {
			LPGEdgeValue edgeValue = (LPGEdgeValue)(record.getEntry(2).getValue());
			System.out.print(edgeValue.getEdge().getId());
			System.out.print(",");
			System.out.print((LiteralValue)(record.getEntry(0).getValue()));
			System.out.print(",");
			System.out.print((LiteralValue)(record.getEntry(3).getValue()));
			System.out.print(",");
			System.out.print((LiteralValue)(record.getEntry(1).getValue()));
			System.out.println();
			PropertyMap properties = edgeValue.getEdge().getProperties();
			for (String name: properties.getPropertyNames()) {
				System.out.print(edgeValue.getEdge().getId());
				System.out.print(",");
				System.out.print(name);
				System.out.print(",");
				System.out.print(properties.getValueFor(name));
			}
			System.out.println();
		}
		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
		}
	}

}
