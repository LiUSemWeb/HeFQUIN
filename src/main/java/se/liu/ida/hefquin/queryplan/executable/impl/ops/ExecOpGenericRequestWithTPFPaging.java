package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;

/**
 * Base class for implementations of paging-based request operators
 * where pages are {@link TPFResponse} objects.
 */
public abstract class ExecOpGenericRequestWithTPFPaging<
                                  ReqType extends DataRetrievalRequest,
                                  MemberType extends FederationMember,
                                  PageReqType extends DataRetrievalRequest>
       extends ExecOpGenericRequestWithPaging<ReqType,MemberType,PageReqType,TPFResponse>
{
	public ExecOpGenericRequestWithTPFPaging( final ReqType req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	protected void consumeResponse( final TPFResponse response, final IntermediateResultElementSink sink ) {
		consumeMatchingTriples( response.getPayloadIterator(), sink );
	}

	@Override
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

	protected void consumeMatchingTriples( final Iterator<Triple> itTriples, final IntermediateResultElementSink sink ) {
		final Iterator<? extends SolutionMapping> itSolMaps = convert( itTriples );
		while ( itSolMaps.hasNext() ) {
			sink.send( itSolMaps.next() );
		}
	}

	protected abstract Iterator<? extends SolutionMapping> convert( Iterator<? extends Triple> itTriples );
}
