package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;

/**
 * A base class for all variations of our batch-based bind-join algorithm
 * that use some form of SPARQL requests.
 */
public abstract class BaseForExecOpParallelBindJoinSPARQL
		extends BaseForExecOpParallelBindJoinWithRequests<SPARQLGraphPattern,
		                                                  SPARQLEndpoint,
		                                                  SPARQLRequest,
		                                                  SolMapsResponse>
{
	public BaseForExecOpParallelBindJoinSPARQL( final SPARQLGraphPattern p,
	                                            final SPARQLEndpoint fm,
	                                            final ExpectedVariables inputVars,
	                                            final boolean useOuterJoinSemantics,
	                                            final int batchSize,
	                                            final boolean collectExceptions,
	                                            final QueryPlanningInfo qpInfo ) {
		super(p, p.getAllMentionedVariables(), fm, inputVars, useOuterJoinSemantics, batchSize, collectExceptions, qpInfo);
	}

	@Override
	protected Iterable<SolutionMapping> extractSolMaps( final SolMapsResponse response )
			throws UnsupportedOperationDueToRetrievalError {
		return response.getResponseData();
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOpForAll() {
		return new ExecOpRequestSPARQL<>( new SPARQLRequestImpl(query), fm, false, null );
	}
}
