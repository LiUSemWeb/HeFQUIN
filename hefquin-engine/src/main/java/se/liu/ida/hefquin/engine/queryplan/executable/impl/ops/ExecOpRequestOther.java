package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
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
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;

public class ExecOpRequestOther extends BaseForExecOpRequest<SPARQLRequest,
                                                             WrappedRESTEndpoint>
{
	private long timeAfterResponse = 0L;
	private long numberOfOutputMappingsProduced = 0L;

	public ExecOpRequestOther( final SPARQLRequest req,
	                           final WrappedRESTEndpoint fm,
	                           final boolean collectExceptions,
	                           final QueryPlanningInfo qpInfo ) {
		super(req, fm, collectExceptions, qpInfo);

		assert fm.getNumberOfParameters() != 0;
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

		// If the given response captures the fact that an error was
		// returned for the REST request, then there are no solution
		// mappings to produce here and, thus, we can immediately
		// stop at this point.
		if ( response.isError() ) {
			return;
		}

		final String data;
		try {
			data = response.getResponseData();
		}
		catch ( final UnsupportedOperationDueToRetrievalError e ) {
			// We should never end up here because we have explicitly
			// checked for a potential error before.
			throw new IllegalStateException("Unexpected exception at this point.", e);
		}

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
