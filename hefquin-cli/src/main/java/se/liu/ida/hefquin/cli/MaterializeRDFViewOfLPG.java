package se.liu.ida.hefquin.cli;

import arq.cmdline.CmdARQ;
import arq.cmdline.ModLangOutput;
import arq.cmdline.ModTime;

import org.apache.jena.atlas.RuntimeIOException;
import org.apache.jena.cmd.ArgDecl;
import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.TypeMapper;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.RDFWriter;
import org.apache.jena.riot.RIOT;
import org.apache.jena.riot.system.StreamRDF;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.apache.jena.sparql.core.Quad;
import org.apache.jena.sparql.graph.GraphFactory;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.cli.modules.ModLPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.access.Neo4jInterface;
import se.liu.ida.hefquin.engine.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.Neo4jRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.conf.LPG2RDFConfiguration;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.PropertyMap;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.Value;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.ArrayValue;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGEdgeValue;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNodeValue;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LiteralValue;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.CypherQuery;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.AliasedExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.AllLabelsExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.CypherVar;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.TypeExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.expression.VariableIDExpression;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.EdgeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.query.impl.match.NodeMatchClause;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.utils.CypherQueryBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

public class MaterializeRDFViewOfLPG extends CmdARQ
{
	protected final ModTime modTime =            new ModTime();
	protected final ModLangOutput modLangOut =   new ModLangOutput();
	protected final ModLPG2RDFConfiguration modLPG2RDFConfiguration = new ModLPG2RDFConfiguration();

	protected final ArgDecl argEndpointURI   = new ArgDecl(ArgDecl.HasValue, "endpoint");


	public static void main( final String[] args ) {
		new MaterializeRDFViewOfLPG(args).mainRun();
	}

	protected MaterializeRDFViewOfLPG( final String[] argv ) {
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
		if ( ! hasArg(argEndpointURI) ) {
			System.err.println( "Error: URI of Neo4j endpoint not specified.");
			System.err.println( "       Specify it using the --" + argEndpointURI.getKeyName() + " argument.");
			return;
		}

		final String neo4jEndpointURI = getArg(argEndpointURI).getValue();
		final Neo4jInterface neo4jIface = new Neo4jInterfaceImpl(neo4jEndpointURI);
		final Neo4jServer neo4jServer = new Neo4jServer() {
			@Override public Neo4jInterface getInterface() { return neo4jIface; }
			@Override public VocabularyMapping getVocabularyMapping() { return null; }
		};

		final LPG2RDFConfiguration l2rConf = modLPG2RDFConfiguration.getLPG2RDFConfiguration();

		// set up the output stream
		final OutputStream outStreamBase = System.out;
		final StreamRDF rdfOutStream = setupOutputStream(outStreamBase);
		rdfOutStream.start();

		final CypherQuery getNodesQuery = buildGetNodesQuery();
		final CypherQuery getEdgesQuery = buildGetEdgesQuery();
		final Neo4jRequestProcessor proc = new Neo4jRequestProcessorImpl();

		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}

		final RecordsResponse nodesResponse = execQuery(getNodesQuery, neo4jServer, proc);
		writeTriplesForNodes(nodesResponse, l2rConf, rdfOutStream);

		final RecordsResponse edgesResponse = execQuery(getEdgesQuery, neo4jServer, proc);
		writeTriplesForEdges(edgesResponse, l2rConf, rdfOutStream);

		rdfOutStream.finish();

		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Overall Processing Time: " + modTime.timeStr(time) + " sec");
		}
	}

	public CypherQuery buildGetNodesQuery() {
		// MATCH (n)
		// RETURN n AS node, HEAD(LABELS(n)) AS label

		final CypherVar n = new CypherVar("n");

		final CypherVar node  = new CypherVar("node");
		final CypherVar label = new CypherVar("label");

		return new CypherQueryBuilder()
				.addMatch( new NodeMatchClause(n) )
				.addReturn( new AliasedExpression(n, node) )
				.addReturn( new AliasedExpression(new AllLabelsExpression(n), label) )
				.build();
	}

	public CypherQuery buildGetEdgesQuery() {
		// MATCH (n1)-[e]->(n2)
		// RETURN ID(n1) AS nid1, ID(n2) AS nid2, e AS edge, TYPE(e) AS reltype

		final CypherVar n1 = new CypherVar("n1");
		final CypherVar n2 = new CypherVar("n2");
		final CypherVar e = new CypherVar("e");

		final CypherVar nid1 = new CypherVar("nid1");
		final CypherVar nid2 = new CypherVar("nid2");
		final CypherVar edge = new CypherVar("edge");
		final CypherVar reltype = new CypherVar("reltype");

		return new CypherQueryBuilder()
				.addMatch( new EdgeMatchClause(n1, e, n2) )
				.addReturn( new AliasedExpression(new VariableIDExpression(n1), nid1) )
				.addReturn( new AliasedExpression(new VariableIDExpression(n2), nid2) )
				.addReturn( new AliasedExpression(e, edge) )
				.addReturn( new AliasedExpression(new TypeExpression(e), reltype) )
				.build();
	}

	protected RecordsResponse execQuery( final CypherQuery query,
	                                     final Neo4jServer server,
	                                     final Neo4jRequestProcessor proc ) {
		final Neo4jRequest request = new Neo4jRequestImpl( query.toString() );

		final RecordsResponse response;
		try {
			response = proc.performRequest(request, server);
		}
		catch ( final Exception ex ) {
			System.out.flush();
			System.err.println( "Executing a Cypher query caused an exception: " + ex.getMessage() );
			System.err.println();
			System.err.println( "Stack Trace: " );
			ex.printStackTrace( System.err );
			throw new IllegalStateException(ex);
		}

		return response;
	}

	protected void writeTriplesForNodes( final RecordsResponse nodesResponse,
	                                     final LPG2RDFConfiguration l2rConf,
	                                     final StreamRDF rdfOutStream ) {
		for ( final TableRecord record : nodesResponse.getResponse() ) {
			// Obtain the relevant values from the current row of the
			// query result, which capture the current node and its label.
			final LPGNodeValue nodeValue = (LPGNodeValue) record.getEntry(0).getValue();
			final ArrayValue labelArray = (ArrayValue) record.getEntry(1).getValue();

			final LPGNode node = nodeValue.getNode();
			final Node termForNode = l2rConf.getRDFTermForLPGNode(node);

			// Create and write the triples that capture the labels of
			// the current LPG node, which are triples in the set that
			// is called nl in the paper.
			for ( final LiteralValue labelValue : labelArray.getElements() ) {
				final String label = labelValue.getValue().toString();
				final Triple t = Triple.create( termForNode,
				                                l2rConf.getLabelPredicate(),
				                                l2rConf.getRDFTermForNodeLabel(label) );
				rdfOutStream.triple(t);
			}

			// Create and write the triples that capture the properties of
			// the current LPG node, which are triples in the set that is
			// called np in the paper.
			writeTriplesForProperties( termForNode, node.getProperties(), l2rConf, rdfOutStream );
		}
	}

	protected void writeTriplesForEdges( final RecordsResponse edgesResponse,
	                                     final LPG2RDFConfiguration l2rConf,
	                                     final StreamRDF rdfOutStream ) {
		for ( final TableRecord record : edgesResponse.getResponse() ) {
			// Obtain the relevant values from the current row of the
			// query result, which capture the current edge, its label,
			// and its incident nodes.
			final LiteralValue srcNodeID = (LiteralValue) record.getEntry(0).getValue();
			final LiteralValue tgtNodeID = (LiteralValue) record.getEntry(1).getValue();
			final LPGEdgeValue edgeValue = (LPGEdgeValue)record.getEntry(2).getValue();
			final LiteralValue labelValue = (LiteralValue) record.getEntry(3).getValue();

			final LPGNode srcNode = new LPGNode( srcNodeID.getValue().toString(), null, null );
			final LPGNode tgtNode = new LPGNode( tgtNodeID.getValue().toString(), null, null );
			final String label = labelValue.getValue().toString();

			// Create the triple that captures the current edge, which is
			// one of the triples in the set that is called e in the paper.
			final Triple edgeTriple = Triple.create( l2rConf.getRDFTermForLPGNode(srcNode),
			                                         l2rConf.getIRIForEdgeLabel(label),
			                                         l2rConf.getRDFTermForLPGNode(tgtNode) );
			rdfOutStream.triple(edgeTriple);

			// Create and write the triples that capture the properties of
			// of the current edge, which are triples in the set that is
			// called ep in the paper.
			writeTriplesForProperties( NodeFactory.createTripleNode(edgeTriple),
			                           edgeValue.getEdge().getProperties(),
			                           l2rConf,
			                           rdfOutStream );
		}
	}

	/**
	 * Creates and writes a triple for each of the properties of the given
	 * {@link PropertyMap} using the given subject for these triples.
	 *
	 * @param subject - used as subject of the created triples
	 * @param properties - the properties for which the triples are created
	 * @param l2rConf - LPG-to-RDF configuration that specifies the IRIs for the property names
	 * @param rdfOutStream - stream to which the created triples are written
	 */
	protected void writeTriplesForProperties( final Node subject,
	                                          final PropertyMap properties,
	                                          final LPG2RDFConfiguration l2rConf,
	                                          final StreamRDF rdfOutStream ) {
		for ( final String pn : properties.getPropertyNames() ) {
			final Value pv = properties.getValueFor(pn);

			// TODO: Capture array values as some form of list
			if ( pv instanceof ArrayValue )
				continue;

			// Create the RDF literal for the value of the current property,
			// to be used as the object of the triple created for the property.
			final LiteralValue lpv = (LiteralValue) pv;
			final RDFDatatype dt = TypeMapper.getInstance().getTypeByValue( lpv.getValue() );
			final Node lit;
			if ( dt == null )
				lit = NodeFactory.createLiteral( lpv.toString() );
			else
				lit = NodeFactory.createLiteralByValue( lpv.getValue(), dt );

			// Create and write the triple.
			final Triple t = Triple.create( subject, l2rConf.getIRIForPropertyName(pn), lit );
			rdfOutStream.triple(t);
		}
	}

	protected StreamRDF setupOutputStream( final OutputStream outStreamBase ) {
		final OutputStream outStream;
		if ( modLangOut.compressedOutput() ) {
			try {
				outStream = new GZIPOutputStream(outStreamBase, true);
			}
			catch ( final IOException e ) {
				throw new RuntimeIOException("Setting up the GZIPOutputStream caused an exception.", e);
			}
        }
		else {
			outStream = outStreamBase;
		}

		final RDFFormat streamFmt = modLangOut.getOutputStreamFormat();
		if ( streamFmt != null )
			return StreamRDFWriter.getWriterStream( outStream, streamFmt, RIOT.getContext() );

		final RDFFormat fmt = modLangOut.getOutputFormatted();
		return new MyCachingStreamRDF(fmt, outStream);
	}

	/**
	 * Collects the emitted triples into a graph and writes this graph to the
	 * given {@link OutputStream} upon calling the {@link #finish()} function.
	 */
	protected static class MyCachingStreamRDF implements StreamRDF {
		protected final RDFFormat fmt;
		protected final OutputStream out;
		protected final Graph graph = GraphFactory.createGraphMem();

		public MyCachingStreamRDF( final RDFFormat fmt, final OutputStream out ) {
			this.fmt = fmt;
			this.out = out;
		}

		@Override
		public void start() {}  // nothing to do here

		@Override
		public void triple( final Triple t ) { graph.add(t); }

		@Override
		public void quad( final Quad q ) { throw new UnsupportedOperationException(); }

		@Override
		public void base( final String b ) { throw new UnsupportedOperationException(); }

		@Override
		public void prefix( final String prefix, final String iri ) { throw new UnsupportedOperationException(); }

		@Override
		public void finish() {
			final RDFWriter writer = RDFWriter.create()
					.format(fmt)
					.source(graph)
					.build();
			writer.output(out);
		}
	}

}
