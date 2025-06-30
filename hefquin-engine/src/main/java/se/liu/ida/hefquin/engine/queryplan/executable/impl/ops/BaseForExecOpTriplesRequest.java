package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;

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
		final Iterable<Triple> triples;
		try {
			triples = response.getResponseData();
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			throw new ExecOpExecutionException( "Accessing the response caused an exception that indicates a data retrieval error (message: " + e.getMessage() + ").", e, this );
		}

		final Iterator<SolutionMapping> it = convert(triples);
		sink.send(it);
	}

	protected abstract TriplesResponse performRequest( final FederationAccessManager fedAccessMgr );

	protected abstract Iterator<SolutionMapping> convert( final Iterable<Triple> itTriples );

}
