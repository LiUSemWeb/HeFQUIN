package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class BaseForExecOpSolMapsRequest<ReqType extends DataRetrievalRequest,
                                                  MemberType extends FederationMember>
                extends BaseForExecOpRequest<ReqType,MemberType>
{
	private long timeAfterResponse = 0L;
	private long solMapsRetrieved = 0L;
	private long numberOfOutputMappingsProduced = 0L;

	public BaseForExecOpSolMapsRequest( final ReqType req, final MemberType fm, final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected final void _execute( final IntermediateResultElementSink sink, final ExecutionContext execCxt )
		throws ExecOpExecutionException
	{
		final SolMapsResponse response;
		try {
			response = performRequest( execCxt.getFederationAccessMgr() );
		}
		catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException( "Performing the request caused an exception.", e, this );
		}

		timeAfterResponse = System.currentTimeMillis();
		try {
			solMapsRetrieved = response.getSize();
		} catch ( final UnsupportedOperationDueToRetrievalError e ) {
			throw new ExecOpExecutionException( "Accessing the response size caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e, this );
		}

		process(response, sink);
	}

	protected void process( final SolMapsResponse response, final IntermediateResultElementSink sink )
		throws ExecOpExecutionException
	{
		final Iterable<SolutionMapping> solutionMappings;;
		try {
			solutionMappings = response.getResponseData();
		}
		catch ( final UnsupportedOperationDueToRetrievalError e ) {
			throw new ExecOpExecutionException( "Accessing the response caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e, this );
		}

		final int cnt = sink.send(solutionMappings);
		numberOfOutputMappingsProduced += cnt;
	}

	protected abstract SolMapsResponse performRequest( FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	@Override
	public void resetStats() {
		super.resetStats();
		timeAfterResponse = 0L;
		solMapsRetrieved = 0L;
		numberOfOutputMappingsProduced = 0L;
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "requestExecTime",                Long.valueOf( timeAtExecEnd - timeAfterResponse ) );
		s.put( "responseProcTime",               Long.valueOf( timeAfterResponse - timeAtExecStart ) );
		s.put( "solMapsRetrieved",               Long.valueOf( solMapsRetrieved ) );
		s.put( "numberOfOutputMappingsProduced", Long.valueOf( numberOfOutputMappingsProduced ) );
		return s;
	}
}
