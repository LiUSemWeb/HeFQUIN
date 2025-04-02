package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TriplesResponse;
import se.liu.ida.hefquin.engine.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Base class for implementations of request operators with
 * requests that return triples and that can be performed as
 * is. The latter means that such requests do not have to be
 * broken into multiple requests as would be the case when
 * interacting with, e.g., a TPF server which employs paging.
 * For interactions with TPF servers, there is a different
 * base class: {@link BaseForExecOpTriplePatternRequestWithTPF}.
 */
public abstract class BaseForExecOpTriplesRequest<ReqType extends DataRetrievalRequest, MemberType extends FederationMember>
                extends BaseForExecOpRequest<ReqType,MemberType>
{
	public BaseForExecOpTriplesRequest( final ReqType req, final MemberType fm, final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected void _execute( final IntermediateResultElementSink sink,
	                         final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		final TriplesResponse response = performRequest( execCxt.getFederationAccessMgr() );

		try {
			final Iterator<? extends SolutionMapping> it = convert( response.getResponseData() );
			while ( it.hasNext() ) {
				sink.send( it.next() );
			}
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			throw new ExecOpExecutionException( e.getMessage(), e, this );
		}
	}

	protected abstract TriplesResponse performRequest( final FederationAccessManager fedAccessMgr );

	protected abstract Iterator<? extends SolutionMapping> convert( final Iterable<? extends Triple> itTriples );

}
