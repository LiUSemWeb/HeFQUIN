package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.query.SolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public abstract class ExecOpGenericSolMapsRequest<ReqType extends DataRetrievalRequest>
                implements NullaryExecutableOp
{
	protected final ReqType req;
	protected final FederationMember fm;

	public ExecOpGenericSolMapsRequest( final ReqType req, final FederationMember fm ) {
		assert req != null;
		assert fm != null;

		this.req = req;
		this.fm = fm;
	}

	public void execute( final IntermediateResultElementSink sink,
	                     final ExecutionContext execCxt )
	{
		final SolMapsResponse response = performRequest( execCxt.getFederationAccessMgr() );
		final Iterator<SolutionMapping> it = response.getIterator();
		while ( it.hasNext() ) {
			sink.send( it.next() );
		}
	}

	abstract protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr );
}
