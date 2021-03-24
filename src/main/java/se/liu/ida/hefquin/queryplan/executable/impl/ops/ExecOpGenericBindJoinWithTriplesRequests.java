package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.query.Query;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;

import java.util.Iterator;
import java.util.Set;

public abstract class ExecOpGenericBindJoinWithTriplesRequests<QueryType extends Query,
																MemberType extends FederationMember,
                                                                           ReqType extends DataRetrievalRequest>
           extends ExecOpGenericBindJoin<QueryType,MemberType>
{
	public ExecOpGenericBindJoinWithTriplesRequests(final QueryType query, final MemberType fm ) {
		super( query, fm );
	}

	@Override
	public void concludeExecution( final IntermediateResultElementSink sink,
						 final ExecutionContext execCxt )
	{
		//final Set<SolutionMapping> solMaps= CollectionOfSM(sink);
		final ReqType req = createRequest(query);
		final TriplesResponse response = performRequest( req, execCxt.getFederationAccessMgr() );

		final Iterator<? extends SolutionMapping> it = convert( response.getIterator(), req );
		while ( it.hasNext() ) {
			sink.send( it.next() );
		}
	}

	protected abstract ReqType createRequest( final QueryType query);

	protected abstract TriplesResponse performRequest( final ReqType req, final FederationAccessManager fedAccessMgr );

	protected abstract Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples, final ReqType req );
}
