package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;

/**
 * Base class for implementations of request operators with triple
 * pattern requests that are broken into TPF requests to handle paging.
 */
public abstract class ExecOpGenericTriplePatternRequestWithTPF<MemberType extends FederationMember>
       extends ExecOpGenericRequestWithTPFPaging<TriplePatternRequest,MemberType,TPFRequest>
{
	public ExecOpGenericTriplePatternRequestWithTPF( final TriplePatternRequest req, final MemberType fm ) {
		super( req, fm );
	}

	@Override
	protected TPFRequest createPageRequest( final int pageNumber ) {
		return new TPFRequestImpl( req.getQueryPattern(), pageNumber );
	}

	@Override
	protected Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getQueryPattern() );
	}
}
