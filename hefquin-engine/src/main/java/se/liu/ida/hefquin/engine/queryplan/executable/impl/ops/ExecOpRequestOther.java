package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.sparql.exec.http.Params;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.jenaintegration.sparql.HeFQUINConstants;

public class ExecOpRequestOther extends BaseForExecOpRequest<SPARQLRequest,RESTEndpoint>
{
	private long timeAfterResponse = 0L;
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpRequestOther( final SPARQLRequest req,
	                           final RESTEndpoint fm,
	                           final boolean collectExceptions,
	                           final QueryPlanningInfo qpInfo ) {
		super(req, fm, collectExceptions, qpInfo);
	}

	@Override
	protected final void _execute( final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt )
		throws ExecOpExecutionException
	{
		final StringResponse response;
		try {
			response = FederationAccessUtils.performRequest( execCxt.getFederationAccessMgr(),
			                                                 createRESTRequest(),
			                                                 fm );
		}
		catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException( "Performing the request caused an exception.", e, this );
		}

		timeAfterResponse = System.currentTimeMillis();

		final String data;
		try {
			data = response.getResponseData();
		} catch ( final UnsupportedOperationDueToRetrievalError e ) {
			throw new ExecOpExecutionException( "Accessing the response data caused an exception, which indicates a data retrieval error (message: " + e.getMessage() + ").", e, this );
		}

		process(data, sink);
	}

	protected RESTRequest createRESTRequest() {
		// TODO:
		final List<Node> paramValues = List.of(
				NodeFactory.createLiteralByValue( 52.52f ), // latitude
				NodeFactory.createLiteralByValue( 13.41f ), // longitude
				NodeFactory.createLiteralByValue( "temperature_2m,wind_speed_10m" ) ); // current
		final Iterator<Node> it = paramValues.iterator();

		final Params params = Params.create();
		for ( final RESTEndpoint.Parameter pDecl : fm.getParameters() ) {
			if ( ! it.hasNext() )
				throw new IllegalArgumentException();

			final Node v = it.next();
			if ( ! v.getLiteralDatatype().equals(pDecl.getType()) )
				throw new IllegalArgumentException();

			final String value = v.getLiteralValue().toString();

			params.add( pDecl.getName(), value );
		}

		return new RESTRequestImpl( fm.getURL(), params );
	}

	protected void process( final String data, final IntermediateResultElementSink sink )
		throws ExecOpExecutionException
	{
		final ObjectMapper mapper = new ObjectMapper();
		final JsonNode rootNode;
		try {
			rootNode = mapper.readTree(data);
		}
		catch ( final JsonProcessingException e ) {
			throw new ExecOpExecutionException( "Failure parsing a JSON response: " + e.getMessage(), e, this );
		}

		final JsonNode currentNode = rootNode.get("current");
		final JsonNode tempNode = currentNode.get("temperature_2m");
		final JsonNode windNode = currentNode.get("wind_speed_10m");

		final double temp = tempNode.asDouble();
		final double wind = windNode.asDouble();

		final Dataset ds = DatasetFactory.create();
		final Graph g = ds.asDatasetGraph().getDefaultGraph();
		final Node bn = NodeFactory.createBlankNode();
		g.add( bn,
		       NodeFactory.createURI("http://example.org/temperature"),
		       NodeFactory.createLiteralByValue(temp) );
		g.add( bn,
		       NodeFactory.createURI("http://example.org/windSpeed"),
		       NodeFactory.createLiteralByValue(wind) );

		final Query q = new Query();
		q.setQuerySelectType();
		q.setQueryResultStar(true);
		q.setQueryPattern( QueryPatternUtils.convertToJenaElement(req.getQueryPattern()) );

		final QueryExecution qe = QueryExecution
				.dataset(ds)
				.query(q)
				.set(HeFQUINConstants.sysExecuteWithJena, true)
				.build();

		final ResultSet rs = qe.execSelect();
		final List<SolutionMapping> solmaps = new ArrayList<>();
		while ( rs.hasNext() ) {
			final SolutionMapping sm = new SolutionMappingImpl( rs.nextBinding() );
			solmaps.add(sm);
		}

		rs.close();

		final int cnt = sink.send(solmaps);
		numberOfOutputMappingsProduced += cnt;
	}

	@Override
	public void resetStats() {
		super.resetStats();
		timeAfterResponse = 0L;
		numberOfOutputMappingsProduced = 0L;
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "requestExecTime",                Long.valueOf( timeAtExecEnd - timeAfterResponse ) );
		s.put( "responseProcTime",               Long.valueOf( timeAfterResponse - timeAtExecStart ) );
		s.put( "numberOfOutputMappingsProduced", Long.valueOf( numberOfOutputMappingsProduced ) );
		return s;
	}
}
