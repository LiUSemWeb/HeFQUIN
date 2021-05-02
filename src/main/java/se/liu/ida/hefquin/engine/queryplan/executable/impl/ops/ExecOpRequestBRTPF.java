package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;

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
	protected Iterator<? extends SolutionMapping> convert( final Iterable<? extends Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getTriplePattern() );
	}
}
