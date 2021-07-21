package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessingException;
import se.liu.ida.hefquin.engine.federation.access.ResponseProcessor;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.*;
import se.liu.ida.hefquin.engine.federation.access.impl.response.CardinalityResponseImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.engine.query.impl.SPARQLQueryImpl;

/**
 * Abstract base class for implementations of the {@link FederationAccessManager}
 * interface that use request processors (see {@link RequestProcessor} etc).
 */
public abstract class FederationAccessManagerBase implements FederationAccessManager
{
	protected final SPARQLRequestProcessor    reqProcSPARQL;
	protected final TPFRequestProcessor       reqProcTPF;
	protected final BRTPFRequestProcessor     reqProcBRTPF;
	protected final Neo4jRequestProcessor     reqProcNeo4j;

	protected FederationAccessManagerBase(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j )
	{
		assert reqProcSPARQL  != null;
		assert reqProcTPF     != null;
		assert reqProcBRTPF   != null;
		assert reqProcNeo4j	  != null;

		this.reqProcSPARQL    = reqProcSPARQL;
		this.reqProcTPF       = reqProcTPF;
		this.reqProcBRTPF     = reqProcBRTPF;
		this.reqProcNeo4j     = reqProcNeo4j;
	}

	@Override
	public void issueCardinalityRequest(
			final SPARQLRequest req,
			final SPARQLEndpoint fm,
			final ResponseProcessor<CardinalityResponse> respProc )
					throws FederationAccessException
	{
		// The idea of this implementation is to take the graph pattern of the
		// given request, wrap it in a COUNT(*) query, and send that query as
		// a request to the given endpoint.
		final SPARQLGraphPattern pattern = req.getQueryPattern();
		if ( pattern == null )
			throw new IllegalArgumentException();

		// set up the query as a SELECT query
		final Query countQuery = QueryFactory.create();
		countQuery.setQuerySelectType();

		// set the WHERE clause of the query based on
		// the graph pattern of the given request
		countQuery.setQueryPattern( QueryPatternUtils.convertToJenaElement(pattern) );

		// initialize the SELECT clause of the query
		// (it needs to be a COUNT(*) without DISTINCT,
		//  and we need a variable for it)
		final Expr countExpr = countQuery.allocAggregate( AggregatorFactory.createCount(false) );
		final Var countVar = Var.alloc("__hefquinCountVar");
		countQuery.addResultVar(countVar, countExpr);

		// issue the query as a request, the response will then be processed by smRespProc
		final SPARQLRequest reqCount = new SPARQLRequestImpl( new SPARQLQueryImpl(countQuery) );
		final ResponseProcessor<SolMapsResponse> smRespProc = new SolMapsResponseProcessorForCardinalityRequests(countVar, respProc);
		try {
			issueRequest(reqCount, fm, smRespProc);
		}
		catch ( final FederationAccessException ex ) {
			throw new FederationAccessException("Issuing the count request caused an exception.", ex, req, fm);
		}
	}


	// ---------- HELPER CLASSES ----------

	protected static class SolMapsResponseProcessorForCardinalityRequests
			implements ResponseProcessor<SolMapsResponse>
	{
		protected final Var countVar;
		protected final ResponseProcessor<CardinalityResponse> respProc;

		public SolMapsResponseProcessorForCardinalityRequests(
				final Var countVar,
				final ResponseProcessor<CardinalityResponse> respProc ) {
			this.countVar = countVar;
			this.respProc = respProc;
		}

		@Override
		public void process( final SolMapsResponse smResp ) throws ResponseProcessingException {
			final Iterator<SolutionMapping> it = smResp.getSolutionMappings().iterator();
			final SolutionMapping sm = it.next();
			final Node countValue = sm.asJenaBinding().get(countVar);
			final int cardinality = ( (Integer) countValue.getLiteralValue() ).intValue();

			// create the response object to be returned
			final CardinalityResponse cardResp = new CardinalityResponseImpl(smResp, smResp.getRequest(), cardinality);
			respProc.process(cardResp);
		}

	} // end of helper class SolMapsResponseProcessorForCardinalityRequests

}
