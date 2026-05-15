package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
public class ExecOpRequestTPF<MemberType extends FederationMember>
       extends BaseForExecOpRequestWithTPFPaging<TriplePatternRequest,MemberType,TPFRequest>
{
	private static final Logger log = LoggerFactory.getLogger( ExecOpRequestTPF.class );

	public ExecOpRequestTPF( final TriplePatternRequest req,
	                         final MemberType fm,
	                         final boolean mayReduce,
	                         final boolean collectExceptions,
	                         final QueryPlanningInfo qpInfo ) {
		super(req, fm, mayReduce, collectExceptions, qpInfo);

		log.info( "Initialized ExecOpRequestBRTPF for server {}", fm );
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
