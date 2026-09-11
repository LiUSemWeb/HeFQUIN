package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;

public class ExecOpRequestOther extends BaseForExecOpRequest<SPARQLRequest,
                                                             WrappedRESTEndpoint>
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpRequestOther.class );
	private long timeAfterResponse = 0L;
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpRequestOther( final SPARQLRequest req,
	                           final WrappedRESTEndpoint fm,
	                           final boolean mayReduce,
	                           final boolean collectExceptions,
	                           final QueryPlanningInfo qpInfo ) {
		super(req, fm, mayReduce, collectExceptions, qpInfo);

		assert fm.getNumberOfParameters() == 0;

		log.debug( "Initialized ExecOpRequestOther for {}", fm );
	}

	@Override
	protected final void _execute( final IntermediateResultElementSink sink,
	                               final QueryProcContextExt ctx )
		throws ExecOpExecutionException
	{
		log.debug( "Issuing REST request to endpoint {}", fm.getURLTemplate() );

		final StringResponse response;
		try {
			response = FederationAccessUtils.performRequest( ctx.getFederationAccessMgr(),
			                                                 createRESTRequest(),
			                                                 fm );
		}
		catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException( e.getMessage(), e, this );
		}

		timeAfterResponse = System.currentTimeMillis();

		if ( response.isDefective() ) {
			throw new ExecOpExecutionException( response.getException().getMessage(),
			                                    response.getException(),
			                                    this );
		}

		if ( response.isError() ) {
			final String msg = "Requesting the execution of a query at the " +
					"server with the service URI " + fm.getServiceURI() +
					" resulted in an error with error code " +
					response.getErrorStatusCode() + " and the " +
					"following message: " + response.getErrorDescription();
			throw new ExecOpExecutionException(msg, this);
		}

		final String data;
		try {
			data = response.getResponseData();
		}
		catch ( final UnsupportedOperationDueToRetrievalError e ) {
			throw new IllegalStateException("We should not end up here.", e);
		}

		log.debug( "Received REST response from {}", fm.getURLTemplate() );

		process(data, sink);
	}

	protected RESTRequest createRESTRequest() {
		return new RESTRequestImpl( fm.getURLTemplate() );
	}

	protected void process( final String data, final IntermediateResultElementSink sink )
		throws ExecOpExecutionException
	{
		final List<SolutionMapping> solmaps;
		try {
			solmaps = fm.evaluatePatternOverRDFView( req.getQueryPattern(), data );
		}
		catch ( final DataConversionException e ) {
			final String msg = "Creating RDF from the response of a " +
					"REST request to the endpoint with the service URI " +
					fm.getServiceURI() + " failed with the following " +
					"message: " + e.getMessage();
			throw new ExecOpExecutionException(msg, e, this);
		}

		final int cnt = sink.send(solmaps);
		numberOfOutputMappingsProduced += cnt;
		log.debug( "Processed REST response: produced {} solution mappings", numberOfOutputMappingsProduced );
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
