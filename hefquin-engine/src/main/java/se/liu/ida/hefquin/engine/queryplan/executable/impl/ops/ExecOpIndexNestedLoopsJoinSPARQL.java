package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.concurrent.CompletableFuture;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.VariableByBlankNodeSubstitutionException;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;

public class ExecOpIndexNestedLoopsJoinSPARQL extends BaseForExecOpIndexNestedLoopsJoinWithSolMapsRequests<SPARQLGraphPattern,SPARQLEndpoint,SPARQLRequest>
{
	public ExecOpIndexNestedLoopsJoinSPARQL( final SPARQLGraphPattern query,
	                                         final SPARQLEndpoint fm,
	                                         final boolean useOuterJoinSemantics,
	                                         final boolean collectExceptions ) {
		super( query, fm, collectExceptions );

		// TODO extend this implementation to support outer join semantics similar
		// to how it is implemented in ExecOpGenericIndexNestedLoopsJoinWithRequestOps
		// TODO when done, extend ExecOpIndexNestedLoopsJoinSPARQLTest accordingly
		if ( useOuterJoinSemantics )
			throw new UnsupportedOperationException();
	}

	@Override
	protected SPARQLRequest createRequest( final SolutionMapping sm )
			throws VariableByBlankNodeSubstitutionException
	{
		final SPARQLGraphPattern pattern = query.applySolMapToGraphPattern(sm);
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
