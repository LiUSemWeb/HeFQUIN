package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericSolMapsRequest<ReqType extends DataRetrievalRequest,
                                                  MemberType extends FederationMember>
                extends ExecOpGenericRequest<ReqType,MemberType>
{
	private long timeAfterResponse = 0L;

	public ExecOpGenericSolMapsRequest( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	protected final void _execute( final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final SolMapsResponse response;
		try {
			response = performRequest( execCxt.getFederationAccessMgr() );
		}
		catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException("Performing the request caused an exception.", e, this);
		}

		timeAfterResponse = System.currentTimeMillis();

		process(response, sink);
	}

	protected void process( final SolMapsResponse response, final IntermediateResultElementSink sink )
	{
		for ( SolutionMapping sm : response.getSolutionMappings() ) {
			sink.send( sm );
		}
	}

	protected abstract SolMapsResponse performRequest( FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	@Override
	public void resetStats() {
		super.resetStats();
		timeAfterResponse = 0L;
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "requestExecTime",  Long.valueOf(timeAtExecEnd-timeAfterResponse) );
		s.put( "responseProcTime", Long.valueOf(timeAfterResponse-timeAtExecStart) );
		return s;
	}
}
