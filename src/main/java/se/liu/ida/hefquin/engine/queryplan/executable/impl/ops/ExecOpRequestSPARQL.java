package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;

public class ExecOpRequestSPARQL extends ExecOpGenericSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQL( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
	}

	@Override
	protected void issueRequest( final FederationAccessManager fedAccessMgr ) throws FederationAccessException {
		fedAccessMgr.issueRequest(req, fm, this);
	}

}
