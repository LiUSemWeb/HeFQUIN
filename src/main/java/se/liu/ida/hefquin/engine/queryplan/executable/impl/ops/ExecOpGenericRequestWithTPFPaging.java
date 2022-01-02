package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Base class for implementations of paging-based request operators
 * where pages are {@link TPFResponse} objects.
 */
public abstract class ExecOpGenericRequestWithTPFPaging<
                                  ReqType extends DataRetrievalRequest,
                                  MemberType extends FederationMember,
                                  PageReqType extends DataRetrievalRequest>
       extends ExecOpGenericRequest<ReqType,MemberType>
{
	public ExecOpGenericRequestWithTPFPaging( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	public final void execute( final IntermediateResultElementSink sink,
	                           final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		TPFResponse currentPage = null;
		while ( currentPage == null || ! isLastPage(currentPage) ) {
			// create the request for the next page (which is the first page if currentPage is null)
			final PageReqType pageRequest = createPageRequest(currentPage);

			// perform the page request
			try {
				currentPage = performPageRequest( pageRequest, execCxt.getFederationAccessMgr() );
			}
			catch ( final FederationAccessException e ) {
				throw new ExecOpExecutionException("Issuing a page request caused an exception.", e, this);
			}

			// consume the matching triples retrieved via the page request
			consumeMatchingTriples( currentPage.getPayload(), sink );
		}
	}

	protected PageReqType createPageRequest( final TPFResponse previousPage ) {
		if ( previousPage == null ) {
			return createPageRequest( (String) null );
		}

		final String nextPageURL = previousPage.getNextPageURL();
		if ( nextPageURL == null ) {
			throw new IllegalStateException();
		}

		return createPageRequest(nextPageURL);
	}

	protected boolean isLastPage( final TPFResponse response ) {
		// To check whether the given response is the last page of the TPF
		// we simply consider the page-related metadata in the response. 
		final Boolean isLastPage = response.isLastPage();
		if ( isLastPage != null ) {
			return isLastPage.booleanValue();
		}

		// If there is no such page-related metadata in the response,
		// then we decide based on the number of matching triples in
		// the response: no triples -> last page!
		return ( response.getPayloadSize() == 0 );
	}

	protected void consumeMatchingTriples( final Iterable<Triple> itTriples, final IntermediateResultElementSink sink ) {
		final Iterator<SolutionMapping> itSolMaps = convert( itTriples );
		while ( itSolMaps.hasNext() ) {
			sink.send( itSolMaps.next() );
		}
	}


	protected abstract PageReqType createPageRequest( String nextPageURL );

	protected abstract TPFResponse performPageRequest( PageReqType pageReq, FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	protected abstract Iterator<SolutionMapping> convert( Iterable<Triple> itTriples );
}
