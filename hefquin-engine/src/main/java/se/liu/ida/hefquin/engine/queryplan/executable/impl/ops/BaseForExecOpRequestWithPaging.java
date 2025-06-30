package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;

/**
 * Base class for implementations of request operators with requests
 * that have to be broken into multiple requests to handle paging.
 */
public abstract class BaseForExecOpRequestWithPaging<
                                  ReqType extends DataRetrievalRequest,
                                  MemberType extends FederationMember,
                                  PageReqType extends DataRetrievalRequest,
                                  PageRespType extends DataRetrievalResponse<?>>
                extends BaseForExecOpRequest<ReqType,MemberType>
{
	public BaseForExecOpRequestWithPaging( final ReqType req, final MemberType fm, final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected final void _execute( final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		int pageNumber = 0;
		boolean wasLastPage = false;
		while ( ! wasLastPage ) {

			// create and issue the request for the next page (processing
			// the response is done in the 'process' method below, which
			// may be executed in a separate thread) 
			final PageReqType pageRequest = createPageRequest(pageNumber);
			final PageRespType pageResponse;
			try {
				pageResponse = performPageRequest( pageRequest, execCxt.getFederationAccessMgr() );
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a page request caused an exception.", e, this);
			}

			consumeResponse(pageResponse, sink);
			wasLastPage = isLastPage(pageResponse);

			++pageNumber;
		}
	}

	protected abstract PageReqType createPageRequest( int pageNumber );

	protected abstract PageRespType performPageRequest( PageReqType pageReq, FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	protected abstract void consumeResponse( PageRespType response, IntermediateResultElementSink sink );

	protected abstract boolean isLastPage( PageRespType response );

}
