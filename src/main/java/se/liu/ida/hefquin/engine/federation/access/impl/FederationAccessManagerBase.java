package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.RequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
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

	protected FederationAccessManagerBase(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF )
	{
		assert reqProcSPARQL  != null;
		assert reqProcTPF     != null;
		assert reqProcBRTPF   != null;

		this.reqProcSPARQL    = reqProcSPARQL;
		this.reqProcTPF       = reqProcTPF;
		this.reqProcBRTPF     = reqProcBRTPF;
	}

	@Override
	public CardinalityResponse performCardinalityRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
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

		// issue the query as a request
		final SPARQLRequest reqCount = new SPARQLRequestImpl( new SPARQLQueryImpl(countQuery) );
		final SolMapsResponse smResp = performRequest(reqCount, fm);

		// extract the COUNT value from the response
		final Iterator<SolutionMapping> it = smResp.getSolutionMappings().iterator();
		final SolutionMapping sm = it.next();
		final Node countValue = sm.asJenaBinding().get(countVar);
		final int cardinality = ( (Integer) countValue.getLiteralValue() ).intValue();

		// create the response object to be returned
		return new CardinalityResponseImpl(smResp, reqCount, cardinality);
	}

}
