package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;

public class ExecOpRequestSPARQL extends ExecOpGenericSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQL( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
	}

	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( req, fm );
	}

}
