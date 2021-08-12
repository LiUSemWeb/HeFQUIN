package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;

public class ExecOpIndexNestedLoopsJoinSPARQL extends ExecOpGenericIndexNestedLoopsJoinWithSolMapsRequests<SPARQLGraphPattern,SPARQLEndpoint,SPARQLRequest>
{
	public ExecOpIndexNestedLoopsJoinSPARQL( final SPARQLGraphPattern query, final SPARQLEndpoint fm ) {
		super( query, fm );
	}

	@Override
	protected SPARQLRequest createRequest( final SolutionMapping sm ) {
		final SPARQLGraphPattern pattern = QueryPatternUtils.applySolMapToGraphPattern(sm, query);
		return new SPARQLRequestImpl(pattern);
	}

	@Override
	protected CompletableFuture<SolMapsResponse> issueRequest(
			final SPARQLRequest req,
			final FederationAccessManager fedAccessMgr )
					throws FederationAccessException
	{
		return fedAccessMgr.issueRequest(req, fm);
	}

}
