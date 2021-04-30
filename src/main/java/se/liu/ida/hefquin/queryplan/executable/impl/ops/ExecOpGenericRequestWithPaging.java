package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

/**
 * Base class for implementations of request operators with requests
 * that have to be broken into multiple requests to handle paging.
 */
public abstract class ExecOpGenericRequestWithPaging<
                                  ReqType extends DataRetrievalRequest,
                                  MemberType extends FederationMember,
                                  PageReqType extends DataRetrievalRequest,
                                  PageRespType extends DataRetrievalResponse>
                extends ExecOpGenericRequest<ReqType,MemberType>
{
	public ExecOpGenericRequestWithPaging( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	public void execute( final IntermediateResultElementSink sink,
	                     final ExecutionContext execCxt )
	{
		int pageNumber = 0;
		boolean wasLastPage = false;
		while ( ! wasLastPage ) {
			// create and issue the request for the next page
			final PageReqType pageRequest = createPageRequest(pageNumber);
			final PageRespType response = performRequest( pageRequest, execCxt.getFederationAccessMgr() );

			// consume the data in the retrieved page
			consumeResponse( response, sink );
			wasLastPage = isLastPage(response);
			++pageNumber;
		}
	}

	protected abstract PageReqType createPageRequest( int pageNumber );

	protected abstract PageRespType performRequest( PageReqType pageReq, FederationAccessManager fedAccessMgr );

	protected abstract void consumeResponse( PageRespType response, IntermediateResultElementSink sink );

	protected abstract boolean isLastPage( PageRespType response );
}
