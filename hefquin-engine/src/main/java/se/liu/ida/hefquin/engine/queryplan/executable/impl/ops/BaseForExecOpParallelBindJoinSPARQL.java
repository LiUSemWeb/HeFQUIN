package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * A base class for all variations of our parallelized, batch-based
 * bind-join algorithm that use some form of SPARQL requests.
 */
public abstract class BaseForExecOpParallelBindJoinSPARQL
		extends BaseForExecOpParallelBindJoin<SPARQLGraphPattern,
		                                                  SPARQLEndpoint,
		                                                  SPARQLRequest,
		                                                  SolMapsResponse>
{
	private static final Logger log = LoggerFactory.getLogger( BaseForExecOpParallelBindJoinSPARQL.class );

	public BaseForExecOpParallelBindJoinSPARQL( final SPARQLGraphPattern p,
	                                            final SPARQLEndpoint fm,
	                                            final ExpectedVariables inputVars,
	                                            final boolean useOuterJoinSemantics,
	                                            final boolean mayReduce,
	                                            final int batchSize,
	                                            final boolean collectExceptions,
	                                            final QueryPlanningInfo qpInfo ) {
		super(p, p.getAllMentionedVariables(), fm, inputVars, useOuterJoinSemantics, mayReduce, batchSize, collectExceptions, qpInfo);
	}

	@Override
	protected Iterable<SolutionMapping> extractSolMaps( final SolMapsResponse response )
			throws UnsupportedOperationDueToRetrievalError {
		return response.getResponseData();
	}

	@Override
	protected NullaryExecutableOp createExecutableReqOpForAll() {
		log.debug( "Creating FULL RETRIEVAL SPARQL request for endpoint {}", fm );

		final SPARQLRequest req = new SPARQLRequestImpl(query, null, this.mayReduce);
		return new ExecOpRequestSPARQL<>(req, fm, this.mayReduce, false, null);
	}
}
