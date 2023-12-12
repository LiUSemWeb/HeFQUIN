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
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.RecordEntry;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNode;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.LPGNodeValue;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.impl.MapValue;
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
	protected final ModLangOutput ModLangOut =   new ModLangOutput();
	protected final ModLPG2RDFConfiguration modLPG2RDFConfiguration = new ModLPG2RDFConfiguration();

	protected final ArgDecl argEndpointURI   = new ArgDecl(ArgDecl.HasValue, "endpoint");

	protected final Map<CypherVar, Var> varMap = new HashMap<>();
	protected final CypherVar ret1 = new CypherVar("ret1");
	protected final CypherVar ret2 = new CypherVar("ret2");
	protected final CypherVar ret3 = new CypherVar("ret3");


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

		varMap.put(ret3, Var.alloc("o"));
		varMap.put(ret2, Var.alloc("p"));
		varMap.put(ret1, Var.alloc("s"));

		final CypherQuery getNodesQuery = buildGetNodesQuery();
		final CypherQuery getEdgesQuery = buildGetEdgesQuery();

		final RecordsResponse NodesResponse = performQueryExecution(getNodesQuery);
		final RecordsResponse EdgesResponse = performQueryExecution(getEdgesQuery);

		final List<SolutionMapping> result = performResultTranslation( NodesResponse, conf, getNodesQuery);
		result.addAll(performResultTranslation(EdgesResponse, conf, getEdgesQuery));
	}

	protected RecordsResponse performQueryExecution(CypherQuery query) {
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

		final List<CypherMatchQuery> queries = new ArrayList<>();
		final CypherVarGenerator gen = new CypherVarGenerator();
		final CypherVar a1 = gen.getAnonVar();
		final CypherVar m = gen.getMarkerVar();

		//	MATCH (a1) RETURN '$0' AS m, a1 AS ret1, 'label' AS ret2, HEAD(LABELS(a1)) AS ret3
		queries.add(new CypherQueryBuilder()
				.addMatch(new NodeMatchClause(a1))
				.addReturn(new MarkerExpression(0, m))
				.addReturn(new AliasedExpression(a1, ret1))
				.addReturn(new AliasedExpression(new LiteralExpression("label"), ret2))
				.addReturn(new AliasedExpression(new LabelsExpression(a1), ret3))
				.build());

		CypherQueryBuilder queryBuilder2 = new CypherQueryBuilder();
		final CypherVar k = new CypherVar("k");
		final CypherVar a2 = gen.getAnonVar();
		final CypherVar a3 = gen.getAnonVar();

		//	MATCH (a2) UNWIND [k IN KEYS(a2) | [k, a2[k]]] AS a3 RETURN '$1' AS m, a2 AS ret1, a3[0] AS ret2, a3[1] AS ret3
		final List<CypherExpression> returnExpressions = new ArrayList<>(Arrays.asList(k, new PropertyAccessExpression(a2, "k")));
		queries.add(new CypherQueryBuilder()
				.addMatch(new NodeMatchClause(a2))
				.addIterator(new UnwindIteratorImpl(k, new KeysExpression(a2), new ArrayList<>(), returnExpressions, a3))
				.addReturn(new MarkerExpression(1, m))
				.addReturn(new AliasedExpression(a2, ret1))
				.addReturn(new AliasedExpression(new GetItemExpression(a3, 0), ret2))
				.addReturn(new AliasedExpression(new GetItemExpression(a3, 1), ret3))
				.build());

		return new CypherUnionQueryImpl(queries);
	}
	public CypherQuery buildGetEdgesQuery(){

		final List<CypherMatchQuery> queries = new ArrayList<>();
		final CypherVarGenerator gen = new CypherVarGenerator();
		final CypherVar m = gen.getMarkerVar();

		final CypherVar a1 = gen.getAnonVar();
		final CypherVar a2 = gen.getAnonVar();
		final CypherVar a3 = gen.getAnonVar();

		//	MATCH (a1)-[a2]->(a3) RETURN '$0' AS m, a1 AS ret1, TYPE(a2) AS ret2, a3 AS ret3
		queries.add(
				new CypherQueryBuilder()
				.addMatch(new EdgeMatchClause(a1, a2, a3))
				.addReturn(new MarkerExpression(0, m))
				.addReturn(new AliasedExpression(a1, ret1))
				.addReturn(new AliasedExpression(new TypeExpression(a2), ret2))
				.addReturn(new AliasedExpression(a3, ret3)).build());


		final CypherVar k = new CypherVar("k");
		final CypherVar a4 = gen.getAnonVar();
		final CypherVar a5 = gen.getAnonVar();
		final CypherVar a6 = gen.getAnonVar();
		final CypherVar a7 = gen.getAnonVar();

		//	MATCH (a4)-[a5]->(a6) UNWIND [k IN KEYS(a5) | [k, a5[k]]] AS a7 RETURN '$1' AS m, {s: a4 , e: TYPE(a5), t: a6} AS ret1, a7[0] AS ret2, a7[1] AS ret3
		final List<CypherExpression> returnExpressions = new ArrayList<>(Arrays.asList(k, new PropertyAccessExpression(a5, "k")));
		queries.add(new CypherQueryBuilder()
				.addMatch(new EdgeMatchClause(a4, a5, a6))
				.addIterator(new UnwindIteratorImpl(k, new KeysExpression(a5), new ArrayList<>(), returnExpressions, a7))
				.addReturn(new MarkerExpression(1, m))
				.addReturn(new AliasedExpression(new TripleMapExpression(a4, a5, a6), ret1))
				.addReturn(new AliasedExpression(new GetItemExpression(a7, 0), ret2))
				.addReturn(new AliasedExpression(new GetItemExpression(a7, 1), ret3))
				.build());

		return new CypherUnionQueryImpl(queries);
	}
	private List<AliasedExpression> getRelevantReturnExpressions(final int index, final CypherQuery query) {
		if (query instanceof CypherMatchQuery)
			return ((CypherMatchQuery) query).getReturnExprs();
		return ((CypherUnionQuery) query).getSubqueries().get(index).getReturnExprs();
	}
	public SolutionMapping translateRecord(final TableRecord record, final LPG2RDFConfiguration conf, final CypherQuery query) {
		final BindingBuilder builder = Binding.builder();
		List<AliasedExpression> returnExpressions = getRelevantReturnExpressions (0, query);
		final int n = record.size();
		CypherExpression currentMarker =  null;

		for (int i = 0; i < n; i++) {
			final RecordEntry current = record.getEntry(i);
			final AliasedExpression aExp = returnExpressions.get(i);
			final CypherVar alias = aExp.getAlias();
			final Var var = varMap.get(alias);
			final CypherExpression expression = aExp.getExpression();
			if (aExp instanceof MarkerExpression) {
				final CypherExpression dataMarker = new LiteralExpression(current.getValue().toString());
				if (currentMarker == null) {
					currentMarker = expression;
				}
				if (! currentMarker.equals(dataMarker)) {
					currentMarker = dataMarker;
					returnExpressions = getRelevantReturnExpressions(1, query);
				}
				continue;
			}
			if (expression instanceof CypherVar) {
				builder.add(var, conf.getRDFTermForLPGNode(((LPGNodeValue) current.getValue()).getNode()));
			} else if (expression instanceof TripleMapExpression) {
				final Map<String, Object> map = ((MapValue) current.getValue()).getMap();
				builder.add(var, NodeFactory.createTripleNode(
						conf.getRDFTermForLPGNode((LPGNode) map.get("s")),
						conf.getIRIForEdgeLabel(map.get("e").toString()),
						conf.getRDFTermForLPGNode((LPGNode) map.get("t"))));
			} else if (expression instanceof TypeExpression) {
				builder.add(var, conf.getIRIForEdgeLabel(current.getValue().toString()));
			} else if (expression instanceof GetItemExpression) {
				final int index = ((GetItemExpression) expression).getIndex();
				if (index == 0) {
					builder.add(var, conf.getIRIForPropertyName(current.getValue().toString()));
				} else if (index == 1) {
					builder.add(var, NodeFactory.createLiteral(current.getValue().toString()));
				} else {
					throw new IllegalArgumentException("Invalid Return Statement");
				}
			} else if (expression instanceof LabelsExpression) {
				builder.add(var, conf.getRDFTermForNodeLabel(current.getValue().toString()));
			} else if (expression instanceof PropertyAccessExpression) {
				builder.add(var, NodeFactory.createLiteral(current.getValue().toString()));
			} else if (expression instanceof LiteralExpression) {
				builder.add(var, conf.getLabelPredicate());
			} else {
				throw new IllegalArgumentException("Invalid Return Statement");
			}
		}

		return new SolutionMappingImpl(builder.build());
	}

	protected List<SolutionMapping> performResultTranslation( final RecordsResponse response,
															  final LPG2RDFConfiguration conf,
															  final CypherQuery query) {
		if ( modTime.timingEnabled() ) {
			modTime.startTimer();
		}
		final List<SolutionMapping> results = new ArrayList<>();
		for (final TableRecord record : response.getResponse()) {
			results.add(translateRecord(record, conf, query));
		}
		if ( modTime.timingEnabled() ) {
			final long time = modTime.endTimer();
			System.out.println("Result Translation Time: " + modTime.timeStr(time) + " sec");
		}

		return results;
	}

	protected void serializeOutput( final List<SolutionMapping> result ) {
		//TODO: Serialize Data
		System.out.println("RESULTS");
	}

}
