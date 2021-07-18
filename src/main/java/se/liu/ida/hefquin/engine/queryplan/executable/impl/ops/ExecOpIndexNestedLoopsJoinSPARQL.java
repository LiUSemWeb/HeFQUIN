package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;

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
	protected SolMapsResponse performRequest( final SPARQLRequest req,
	                                          final FederationAccessManager fedAccessMgr )
			throws ExecOpExecutionException
	{
		try {
			return fedAccessMgr.performRequest( req, fm );
		}
		catch ( final FederationAccessException ex ) {
			throw new ExecOpExecutionException("An exception occurred when performing a SPARQL request during this index nested loops join.", ex, this);
		}
	}

}
