package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.federation.access.utils.FederationAccessUtils;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;

public class ExecOpRequestSPARQL extends BaseForExecOpSolMapsRequest<SPARQLRequest, SPARQLEndpoint>
{
	public ExecOpRequestSPARQL( final SPARQLRequest req,
	                            final SPARQLEndpoint fm,
	                            final boolean collectExceptions,
	                            final QueryPlanningInfo qpInfo ) {
		super(req, fm, collectExceptions, qpInfo);
	}

	@Override
	protected SolMapsResponse performRequest( final FederationAccessManager fedAccessMgr ) throws FederationAccessException {
		return FederationAccessUtils.performRequest(fedAccessMgr, req, fm);
	}

}
