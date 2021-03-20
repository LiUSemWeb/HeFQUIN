package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;

public class ExecOpRequestTPFatTPFServer extends ExecOpGenericTPFRequest<TPFServer>
{
	public ExecOpRequestTPFatTPFServer( final TriplePatternRequest req, final TPFServer fm ) {
		super( req, fm );
	}

	@Override
	protected TriplesResponse performRequest( final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( req, fm );
	}

}
