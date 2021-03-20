package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public class ExecOpRequestTPFatBRTPFServer extends ExecOpGenericTPFRequest<BRTPFServer>
{
	public ExecOpRequestTPFatBRTPFServer( final TriplePatternRequest req, final BRTPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected TriplesResponse performRequest( final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( req, fm );
	}

}
