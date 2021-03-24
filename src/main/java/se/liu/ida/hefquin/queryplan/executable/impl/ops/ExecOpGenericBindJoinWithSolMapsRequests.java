package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

import java.util.Iterator;

public abstract class ExecOpGenericBindJoinWithSolMapsRequests<QueryType extends Query,
		MemberType extends FederationMember,
		ReqType extends DataRetrievalRequest>
		extends ExecOpGenericBindJoin<QueryType,MemberType>
{
	public ExecOpGenericBindJoinWithSolMapsRequests(final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	public void concludeExecution( final IntermediateResultElementSink sink,
								   final ExecutionContext execCxt )
	{
		final ReqType req = createRequest(query);
		final SolMapsResponse response = performRequest(req, execCxt.getFederationAccessMgr());
		final Iterator<SolutionMapping> it = response.getIterator();
		while ( it.hasNext() ) {
			sink.send( it.next() );
		}
	}

	protected abstract ReqType createRequest(final QueryType query);
	protected abstract SolMapsResponse performRequest( final ReqType req, final FederationAccessManager fedAccessMgr );
}
