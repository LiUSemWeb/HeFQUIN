package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.utils.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;

/**
 * Base class for implementations of request operators with triple
 * pattern requests that are broken into TPF requests to handle paging.
 */
public abstract class BaseForExecOpTriplePatternRequestWithTPF<MemberType extends FederationMember>
       extends BaseForExecOpRequestWithTPFPaging<TriplePatternRequest,MemberType,TPFRequest>
{
	public BaseForExecOpTriplePatternRequestWithTPF( final TriplePatternRequest req,
	                                                 final MemberType fm,
	                                                 final boolean collectExceptions,
	                                                 final QueryPlanningInfo qpInfo ) {
		super(req, fm, collectExceptions, qpInfo);
	}

	@Override
	protected TPFRequest createPageRequest( final String nextPageURL ) {
		return new TPFRequestImpl( req.getQueryPattern(), nextPageURL );
	}

	@Override
	protected Iterator<SolutionMapping> convert( final Iterable<Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getQueryPattern() );
	}
}
