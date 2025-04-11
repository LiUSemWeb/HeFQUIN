package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ExecutableOperatorStatsImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

/**
 * Base class for implementations of paging-based request operators
 * where pages are {@link TPFResponse} objects.
 */
public abstract class BaseForExecOpRequestWithTPFPaging<
                                  ReqType extends DataRetrievalRequest,
                                  MemberType extends FederationMember,
                                  PageReqType extends DataRetrievalRequest>
       extends BaseForExecOpRequest<ReqType,MemberType>
{
	private int numberOfPageRequestsIssued = 0;
	private int totalNumberOfMatchingTriplesRetrieved = 0;
	private int minNumberOfMatchingTriplesPerPage = Integer.MAX_VALUE;
	private int maxNumberOfMatchingTriplesPerPage = 0;
	private int numberOfOutputMappingsProduced = 0;

	public BaseForExecOpRequestWithTPFPaging( final ReqType req, final MemberType fm, final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected final void _execute( final IntermediateResultElementSink sink,
	                               final ExecutionContext execCxt ) throws ExecOpExecutionException
	{
		try {
			TPFResponse currentPage = null;
			while ( currentPage == null || ! isLastPage( currentPage ) ) {
				// create the request for the next page (which is the first page if currentPage is null)
				final PageReqType pageRequest = createPageRequest( currentPage );

				numberOfPageRequestsIssued++;

				// perform the page request

				currentPage = performPageRequest( pageRequest, execCxt.getFederationAccessMgr() );
				// update stats
				final int payloadSize = currentPage.getPayloadSize();
				totalNumberOfMatchingTriplesRetrieved += payloadSize;
				if ( minNumberOfMatchingTriplesPerPage > payloadSize )
					minNumberOfMatchingTriplesPerPage = payloadSize;
				if ( maxNumberOfMatchingTriplesPerPage < payloadSize )
					maxNumberOfMatchingTriplesPerPage = payloadSize;

				// consume the matching triples retrieved via the page request
				consumeMatchingTriples( currentPage.getPayload(), sink );
			}
		} catch ( final FederationAccessException e ) {
			throw new ExecOpExecutionException( "Issuing a page request caused an exception.", e, this );
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

	protected boolean isLastPage( final TPFResponse response ) throws UnsupportedOperationDueToRetrievalError {
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
			numberOfOutputMappingsProduced++;
			sink.send( itSolMaps.next() );
		}
	}


	protected abstract PageReqType createPageRequest( String nextPageURL );

	protected abstract TPFResponse performPageRequest( PageReqType pageReq, FederationAccessManager fedAccessMgr ) throws FederationAccessException;

	protected abstract Iterator<SolutionMapping> convert( Iterable<Triple> itTriples );

	@Override
	public void resetStats() {
		super.resetStats();
		numberOfPageRequestsIssued = 0;
		totalNumberOfMatchingTriplesRetrieved = 0;
		minNumberOfMatchingTriplesPerPage = 0;
		maxNumberOfMatchingTriplesPerPage = 0;
		numberOfOutputMappingsProduced = 0;
	}

	protected ExecutableOperatorStatsImpl createStats() {
		final ExecutableOperatorStatsImpl s = super.createStats();
		s.put( "numberOfPageRequestsIssued",             Integer.valueOf(numberOfPageRequestsIssued) );
		s.put( "totalNumberOfMatchingTriplesRetrieved",  Integer.valueOf(totalNumberOfMatchingTriplesRetrieved) );
		s.put( "minNumberOfMatchingTriplesPerPage",      Integer.valueOf(minNumberOfMatchingTriplesPerPage) );
		s.put( "maxNumberOfMatchingTriplesPerPage",      Integer.valueOf(maxNumberOfMatchingTriplesPerPage) );
		s.put( "numberOfOutputMappingsProduced",         Integer.valueOf(numberOfOutputMappingsProduced) );
		return s;
	}
}
