package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;

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
	protected Iterator<? extends SolutionMapping> convert( final Iterable<Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getQueryPattern() );
	}
}
