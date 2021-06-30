package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public abstract class ExecOpGenericSolMapsRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                extends ExecOpGenericRequest<ReqType,MemberType>
{
	public ExecOpGenericSolMapsRequest( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	public void execute( final IntermediateResultElementSink sink,
	                     final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final SolMapsResponse response = performRequest( execCxt.getFederationAccessMgr() );
		for ( SolutionMapping sm : response.getSolutionMappings() ) {
			sink.send( sm );
		}
	}

	protected abstract SolMapsResponse performRequest(
			final FederationAccessManager fedAccessMgr ) throws ExecOpExecutionException;
}
