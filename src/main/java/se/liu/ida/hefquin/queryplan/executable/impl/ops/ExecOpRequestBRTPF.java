package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.impl.req.BRTPFRequestImpl;

public class ExecOpRequestBRTPF extends ExecOpGenericRequestWithTPFPaging<BindingsRestrictedTriplePatternRequest,BRTPFServer,BRTPFRequest>
{
	public ExecOpRequestBRTPF( final BindingsRestrictedTriplePatternRequest req, final BRTPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected BRTPFRequest createPageRequest( final int pageNumber ) {
		return new BRTPFRequestImpl( req.getTriplePattern(), req.getSolutionMappings(), pageNumber );
	}

	@Override
	protected TPFResponse performRequest( final BRTPFRequest tpfReq, final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( tpfReq, fm );
	}

	@Override
	protected Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getTriplePattern() );
	}
}
