package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

/**
 * Base class for implementations of request operators with
 * requests that return triples and that can be performed as
 * is. The latter means that such requests do not have to be
 * broken into multiple requests as would be the case when
 * interacting with, e.g., a TPF server which employs paging.
 * For interactions with TPF servers, there is a different
 * base class: {@link ExecOpGenericTriplePatternRequestWithTPF}.
 */
public abstract class ExecOpGenericTriplesRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                extends ExecOpGenericRequest<ReqType,MemberType>
{
	public ExecOpGenericTriplesRequest( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	public void execute( final IntermediateResultElementSink sink,
	                     final ExecutionContext execCxt )
	{
		final TriplesResponse response = performRequest( execCxt.getFederationAccessMgr() );

		final Iterator<? extends SolutionMapping> it = convert( response.getIterator() );
		while ( it.hasNext() ) {
			sink.send( it.next() );
		}
	}

	protected abstract TriplesResponse performRequest( final FederationAccessManager fedAccessMgr );

	protected abstract Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples );

}
