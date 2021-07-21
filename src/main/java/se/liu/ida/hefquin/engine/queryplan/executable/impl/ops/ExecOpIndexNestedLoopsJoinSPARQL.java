package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
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
	protected void issueRequest( final SPARQLRequest req,
	                             final MyResponseProcessor respProc,
	                             final FederationAccessManager fedAccessMgr ) throws FederationAccessException
	{
		fedAccessMgr.issueRequest( req, fm, respProc );
	}

}
