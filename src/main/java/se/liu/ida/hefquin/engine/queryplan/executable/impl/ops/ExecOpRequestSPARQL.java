package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;

public class ExecOpRequestSPARQL extends BaseForExecOpSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQL( final SPARQLRequest req, final SPARQLEndpoint fm ) {
		super( req, fm );
	}

	@Override
	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) throws FederationAccessException {
		return FederationAccessUtils.performRequest(fedAccessMgr, req, fm);
	}

}
