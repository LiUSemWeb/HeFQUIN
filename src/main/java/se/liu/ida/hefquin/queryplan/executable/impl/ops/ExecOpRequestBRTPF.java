package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.Iterator;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.TriplesToSolMapsConverter;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public class ExecOpRequestBRTPF extends ExecOpGenericTriplesRequest<BindingsRestrictedTriplePatternRequest,BRTPFServer>
{
	public ExecOpRequestBRTPF( final BindingsRestrictedTriplePatternRequest req, final BRTPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected TriplesResponse performRequest( final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( req, fm );
	}

	@Override
	protected Iterator<? extends SolutionMapping> convert( final Iterator<? extends Triple> itTriples ) {
		return TriplesToSolMapsConverter.convert( itTriples, req.getTriplePattern() );
	}

}
