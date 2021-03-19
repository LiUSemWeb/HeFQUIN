package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public class ExecOpRequestSPARQL extends ExecOpGenericSolMapsRequest<SPARQLRequest>
{
	public ExecOpRequestSPARQL( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
	}

	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest( req, (SPARQLEndpoint) fm );
	}

}
