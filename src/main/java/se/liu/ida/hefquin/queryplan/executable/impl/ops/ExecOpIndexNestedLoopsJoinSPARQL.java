package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedQueryPatternUtils;

public class ExecOpIndexNestedLoopsJoinSPARQL extends ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests<SPARQLGraphPattern,SPARQLEndpoint,SPARQLRequest>
{
	public ExecOpIndexNestedLoopsJoinSPARQL( final SPARQLGraphPattern query, final SPARQLEndpoint fm ) {
		super( query, fm );
	}

	@Override
	protected SPARQLRequest createRequest( final SolutionMapping sm ) {
		final SPARQLGraphPattern pattern = JenaBasedQueryPatternUtils.applySolMapToGraphPattern(sm, query);
		return new SPARQLRequestImpl(pattern);
	}

	@Override
	protected SolMapsResponse performRequest( final SPARQLRequest req, final FederationAccessManager fedAccessMgr ) {
		return fedAccessMgr.performRequest(req, fm);
	}

}
