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
	protected final void _execute( final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final SolMapsResponse response;
		try {
			response = performRequest( execCxt.getFederationAccessMgr() );
			timeAfterResponse = System.currentTimeMillis();
			solMapsRetrieved = response.getSize();
			process( response, sink );
		} catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException( "Performing the request caused an exception.", e, this );
		}
	}

	protected void process( final SolMapsResponse response, final IntermediateResultElementSink sink )
		throws UnsupportedOperationDueToRetrievalError
	{
		for ( SolutionMapping sm : response.getResponseData() ) {
			numberOfOutputMappingsProduced++;
			sink.send( sm );
		}
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
		s.put( "requestExecTime",  Long.valueOf(timeAtExecEnd-timeAfterResponse) );
		s.put( "responseProcTime", Long.valueOf(timeAfterResponse-timeAtExecStart) );
		s.put( "solMapsRetrieved", Long.valueOf(solMapsRetrieved) );
		s.put( "numberOfOutputMappingsProduced", Long.valueOf(numberOfOutputMappingsProduced) );
		return s;
	}
}
