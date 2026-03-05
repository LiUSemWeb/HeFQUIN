package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.RESTRequestImpl;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint;
import se.liu.ida.hefquin.federation.members.WrappedRESTEndpoint.DataConversionException;

public class ExecOpLookupJoinViaWrapperWithoutParamVars
       extends UnaryExecutableOpBase
{
	protected final SPARQLGraphPattern pattern;
	protected final WrappedRESTEndpoint fm;

	// TODO: It might make sense to use an index-like data structure instead
	// of a simple list, especially if there are join variables (i.e., the
	// intersection of the certain variables of the graph pattern and the
	// certain variables of the sub-plan is not empty) and the cardinality
	// of the input result is estimated to be great.
	protected List<SolutionMapping> solmapsFromRequest = null;

	// statistics
	private long requestExecTime = -1L;
	private long responseProcTime = -1L;
	private long numOfSolMapsFromRequest = -1L;
	private long numberOfOutputMappingsProduced = 0L;
	private String retrievalError = null;
	private String dataConversionError = null;

	public ExecOpLookupJoinViaWrapperWithoutParamVars( final SPARQLGraphPattern pattern,
	                                                   final WrappedRESTEndpoint fm,
	                                                   final boolean collectExceptions,
	                                                   final QueryPlanningInfo qpInfo ) {
		super(collectExceptions, qpInfo);

		assert pattern != null;
		assert fm != null;
		assert fm.getNumberOfParameters() == 0;

		this.pattern = pattern;
		this.fm = fm;
	}

	@Override
	protected void _process( final SolutionMapping inputSM,
	                         final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt )
			throws ExecOpExecutionException {
		if ( solmapsFromRequest == null ) {
			solmapsFromRequest = performRequest(execCxt);
		}

		for ( final SolutionMapping fetchedSM : solmapsFromRequest ) {
			if ( SolutionMappingUtils.compatible(fetchedSM, inputSM) ) {
				final SolutionMapping out = SolutionMappingUtils.merge(inputSM, fetchedSM);
				sink.send(out);
				numberOfOutputMappingsProduced++;
			}
		}
	}

	@Override
	protected void _concludeExecution( final IntermediateResultElementSink sink,
	                                   final ExecutionContext execCxt ) {
		if ( solmapsFromRequest != null )
			solmapsFromRequest.clear();
	}

	protected List<SolutionMapping> performRequest( final ExecutionContext execCxt )
			throws ExecOpExecutionException
	{
		final RESTRequest req = new RESTRequestImpl( fm.getURLTemplate() );

		final long time1 = System.currentTimeMillis();

		final CompletableFuture<StringResponse> f;
		try {
			f = execCxt.getFederationAccessMgr().issueRequest(req, fm);
		}
		catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException("Issuing a request caused an exception.", e, this);
		}

		final StringResponse response;
		try {
			response = f.get();
		}
		catch ( final InterruptedException e ) {
			throw new ExecOpExecutionException("Interruption of the future that performs the request.", e, this);
		}
		catch ( final ExecutionException e ) {
			throw new ExecOpExecutionException("The execution of the futures that performs the request caused an exception.", e, this);
		}

		final long time2 = System.currentTimeMillis();

		final String data;
		try {
			data = response.getResponseData();
		}
		catch ( final UnsupportedOperationDueToRetrievalError e ) {
			retrievalError = e.getMessage();
			return List.of();
		}

		final List<SolutionMapping> result;
		try {
			result = fm.evaluatePatternOverRDFView(pattern, data);
		}
		catch ( final DataConversionException e ) {
			dataConversionError = e.getMessage();
			return List.of();
		}

		final long time3 = System.currentTimeMillis();

		requestExecTime = time2 - time1;
		responseProcTime = time3 - time2;
		numOfSolMapsFromRequest = result.size();
		return result;
	}

	@Override
	public void resetStats() {
		super.resetStats();

		requestExecTime = -1L;
		responseProcTime = -1L;
		numOfSolMapsFromRequest = -1L;
		numberOfOutputMappingsProduced = 0L;
		retrievalError = null;
		dataConversionError = null;
	}

	@Override
	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();

		s.put( "requestExecTime",                requestExecTime );
		s.put( "responseProcTime",               responseProcTime );
		s.put( "numOfSolMapsFromRequest",        numOfSolMapsFromRequest );
		s.put( "numberOfOutputMappingsProduced", numberOfOutputMappingsProduced );
		s.put( "retrievalError",                 retrievalError );
		s.put( "dataConversionError",            dataConversionError );

		return s;
	}

}
