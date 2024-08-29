package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.utils.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;

public class ExecOpRequestBRTPF extends BaseForExecOpRequestWithTPFPaging<BindingsRestrictedTriplePatternRequest,BRTPFServer,BRTPFRequest>
{
	public ExecOpRequestBRTPF( final BindingsRestrictedTriplePatternRequest req,
	                           final BRTPFServer fm,
	                           final boolean collectExceptions ) {
		super( req, fm, collectExceptions );
	}

	@Override
	protected BRTPFRequest createPageRequest( final String nextPageURL ) {
		return new BRTPFRequestImpl( req.getTriplePattern(), req.getSolutionMappings(), nextPageURL );
	}

	@Override
	protected TPFResponse performPageRequest( final BRTPFRequest req,
	                                          final FederationAccessManager fedAccessMgr )
			throws FederationAccessException
	{
		return FederationAccessUtils.performRequest(fedAccessMgr, req, fm);
	}

	@Override
	protected Iterator<SolutionMapping> convert( final Iterable<Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getTriplePattern() );
	}
}
