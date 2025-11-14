package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.exec.http.Params;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.SPARQLOverRESTRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;

public class ExecOpRequestOther extends BaseForExecOpRequest<SPARQLOverRESTRequest,
                                                             WrappedRESTEndpoint>
{
	private long timeAfterResponse = 0L;
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpRequestOther( final SPARQLOverRESTRequest req,
	                           final WrappedRESTEndpoint fm,
	                           final boolean collectExceptions,
	                           final QueryPlanningInfo qpInfo ) {
		super(req, fm, collectExceptions, qpInfo);

		assert req.getParamVars() == null || req.getParamVars().isEmpty();
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
		final List<SolutionMapping> solmaps;
		try {
			solmaps = fm.evaluatePatternOverRDFView( req.getQueryPattern(), data );
		}
		catch ( final DataConversionException e ) {
			throw new ExecOpExecutionException("Converting the reponse of a REST request into RDF failed.", e, this);
		}

		final int cnt = sink.send(solmaps);
		numberOfOutputMappingsProduced += cnt;
	}

	@Override
	public void resetStats() {
		super.resetStats();
		timeAfterResponse = 0L;
		numberOfOutputMappingsProduced = 0L;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "requestExecTime",                Long.valueOf( timeAtExecEnd - timeAfterResponse ) );
		s.put( "responseProcTime",               Long.valueOf( timeAfterResponse - timeAtExecStart ) );
		s.put( "numberOfOutputMappingsProduced", Long.valueOf( numberOfOutputMappingsProduced ) );
		return s;
	}
}
