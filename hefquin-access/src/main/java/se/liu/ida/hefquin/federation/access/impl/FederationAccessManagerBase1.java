package se.liu.ida.hefquin.federation.access.impl;

import java.util.Iterator;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.apache.jena.graph.Node;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.base.query.utils.QueryPatternUtils;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.response.CardinalityResponseImpl;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

/**
 * As a basis for classes that implement the {@link FederationAccessManager}
 * interface, this abstract base class provides default implementations of
 * the methods for issuing cardinality requests. These implementations issue
 * specific data fetching requests via which the desired cardinality estimates
 * can be obtained.
 */
public abstract class FederationAccessManagerBase1 implements FederationAccessManager
{
	public static final String enOverallNumberOfCardRequestsIssued  = "overallNumberOfCardRequestsIssued";
	public static final String enNumberOfSPARQLCardRequestsIssued   = "numberOfSPARQLCardRequestsIssued";
	public static final String enNumberOfTPFCardRequestsIssued      = "numberOfTPFCardRequestsIssued";
	public static final String enNumberOfBRTPFCardRequestsIssued    = "numberOfBRTPFCardRequestsIssued";

	protected static final Var countVar = Var.alloc("__hefquinCountVar");
	protected static final Function<SolMapsResponse, CardinalityResponse> fctToObtainCardinalityResponseFromSolMapsResponse = new FunctionToObtainCardinalityResponseFromSolMapsResponse();
	protected static final Function<TPFResponse, CardinalityResponse> fctToObtainCardinalityResponseFromTPFResponse= new FunctionToObtainCardinalityResponseFromTPFResponse();

	protected AtomicLong issuedCardRequestsSPARQL = new AtomicLong( 0L );
	protected AtomicLong issuedCardRequestsTPF    = new AtomicLong( 0L );
	protected AtomicLong issuedCardRequestsBRTPF  = new AtomicLong( 0L );

	public < ReqType extends DataRetrievalRequest,
	         RespType extends DataRetrievalResponse<?>,
	         MemberType extends FederationMember >
	CompletableFuture<CardinalityResponse> issueCardinalityRequest(
			final ReqType req,
	        final MemberType fm )
					throws FederationAccessException
	{
		final CompletableFuture<CardinalityResponse> response;
		if (    req instanceof TPFRequest tpfReq
		     && fm instanceof TPFServer tpfServer )
			response = _issueCardinalityRequest(tpfReq, tpfServer);
		else if (    req instanceof TPFRequest tpfReq
		          && fm instanceof BRTPFServer brtpfServer )
			response = _issueCardinalityRequest(tpfReq, brtpfServer);
		else if (    req instanceof BRTPFRequest brtpfReq
		          && fm instanceof BRTPFServer brtpfServer )
			response = _issueCardinalityRequest(brtpfReq, brtpfServer);
		else if (    req instanceof SPARQLRequest sparqlReq
		          && fm instanceof SPARQLEndpoint sparqlEndpoint )
			response = _issueCardinalityRequest(sparqlReq, sparqlEndpoint);
		else
			throw new IllegalStateException( "Unsupported request/federation member combination: " +
			                                 req.getClass().getName() + "/" + fm.getClass().getName() );
		return response;
	}

	public CompletableFuture<CardinalityResponse> _issueCardinalityRequest(
			final SPARQLRequest req,
			final SPARQLEndpoint fm )
					throws FederationAccessException
	{
		// The idea of this implementation is to take the graph pattern of the
		// given request, wrap it in a COUNT(*) query, and send that query as
		// a request to the given endpoint.
		final SPARQLGraphPattern pattern = req.getQueryPattern();
		if ( pattern == null ) {
			throw new IllegalArgumentException();
		}

		// set up the query as a SELECT query
		final Query countQuery = QueryFactory.create();
		countQuery.setQuerySelectType();

		// set the WHERE clause of the query based on
		// the graph pattern of the given request
		countQuery.setQueryPattern( QueryPatternUtils.convertToJenaElement( pattern ) );

		// initialize the SELECT clause of the query
		// (it needs to be a COUNT(*) or COUNT(DISTINCT *) depending on
		// whether the request requires duplicate elimination, and we need
		// a variable for the result)
		final Expr countExpr = countQuery.allocAggregate( AggregatorFactory.createCount( req.getDistinctRequired() ) );
		countQuery.addResultVar( countVar, countExpr );

		// issue the query as a request, the response will then be processed to create
		// the CardinalityResponse to be returned
		final SPARQLRequest reqCount = new SPARQLRequestImpl( new SPARQLQueryImpl( countQuery ) );
		final CompletableFuture<SolMapsResponse> ftr = issueRequest(reqCount, fm);
		return ftr.thenApply(fctToObtainCardinalityResponseFromSolMapsResponse);
	}

	public CompletableFuture<CardinalityResponse> _issueCardinalityRequest(
			final TPFRequest req,
			final TPFServer fm )
					throws FederationAccessException
	{
		final CompletableFuture<TPFResponse> ftr = issueRequest(req, fm);
		return ftr.thenApply(fctToObtainCardinalityResponseFromTPFResponse);
	}

	public CompletableFuture<CardinalityResponse> _issueCardinalityRequest(
			final TPFRequest req,
			final BRTPFServer fm )
					throws FederationAccessException
	{
		final CompletableFuture<TPFResponse> ftr = issueRequest(req, fm);
		return ftr.thenApply(fctToObtainCardinalityResponseFromTPFResponse);
	}

	public CompletableFuture<CardinalityResponse> _issueCardinalityRequest(
			final BRTPFRequest req,
			final BRTPFServer fm )
					throws FederationAccessException
	{
		final CompletableFuture<TPFResponse> ftr = issueRequest(req, fm);
		return ftr.thenApply(fctToObtainCardinalityResponseFromTPFResponse);
	}

	@Override
	public final FederationAccessStats getStats() {
		final FederationAccessStatsImpl stats = _getStats();

		final long issuedCardRequestsSPARQL = this.issuedCardRequestsSPARQL.get();
		final long issuedCardRequestsTPF    = this.issuedCardRequestsTPF.get();
		final long issuedCardRequestsBRTPF  = this.issuedCardRequestsBRTPF.get();

		stats.put( enNumberOfSPARQLCardRequestsIssued, Long.valueOf( issuedCardRequestsSPARQL ) );
		stats.put( enNumberOfTPFCardRequestsIssued,    Long.valueOf( issuedCardRequestsTPF ) );
		stats.put( enNumberOfBRTPFCardRequestsIssued,  Long.valueOf( issuedCardRequestsBRTPF ) );

		final long overallCardRequests = issuedCardRequestsSPARQL
		                               + issuedCardRequestsTPF
		                               + issuedCardRequestsBRTPF;
		stats.put( enOverallNumberOfCardRequestsIssued, Long.valueOf( overallCardRequests ) );

		return stats;
	}

	@Override
	public final void resetStats() {
		issuedCardRequestsSPARQL.set( 0L );
		issuedCardRequestsTPF.set( 0L );
		issuedCardRequestsBRTPF.set( 0L );

		_resetStats();
	}

	protected abstract FederationAccessStatsImpl _getStats();

	protected abstract void _resetStats();


	// ---------- HELPER CLASSES ----------

	protected static class FunctionToObtainCardinalityResponseFromSolMapsResponse implements Function<SolMapsResponse, CardinalityResponse>
	{
		public CardinalityResponse apply( final SolMapsResponse smResp ) {
			// First, check that the given response is not defective
			// and does not represent an error response.
			if ( smResp.isError() )
				return new CardinalityResponseImpl( smResp.getErrorStatusCode(),
				                                    smResp.getErrorDescription(),
				                                    smResp.getRequestStartTime(),
				                                    smResp.getRetrievalEndTime() );

			if ( smResp.isDefective() )
				return new CardinalityResponseImpl( smResp.getException(),
				                                    smResp.getRequestStartTime(),
				                                    smResp.getRetrievalEndTime() );

			// Now, we extract the cardinality from
			// the solution mapping of the response.

			final Iterator<SolutionMapping> it;
			try {
				it = smResp.getResponseData().iterator();
			}
			catch ( final UnsupportedOperationDueToRetrievalError e ) {
				throw new IllegalStateException("We should not end up here.", e);
			}

			if ( ! it.hasNext() ) {
				final String msg = "The result obtained for a cardinality-related SPARQL query is unexpectedly empty.";
				return new CardinalityResponseImpl( new IllegalArgumentException(msg),
				                                    smResp.getRequestStartTime(),
				                                    smResp.getRetrievalEndTime() );
			}

			final SolutionMapping sm = it.next();
			final Node countValueNode = sm.asJenaBinding().get(countVar);

			if ( countValueNode == null ) {
				final String msg = "The result obtained for a cardinality-related SPARQL query does not contain the count variable.";
				return new CardinalityResponseImpl( new IllegalArgumentException(msg),
				                                    smResp.getRequestStartTime(),
				                                    smResp.getRetrievalEndTime() );
			}

			if ( ! countValueNode.isLiteral() ) {
				final String msg = "The result obtained for a cardinality-related SPARQL query does not have a literal for the count variable.";
				return new CardinalityResponseImpl( new IllegalArgumentException(msg),
				                                    smResp.getRequestStartTime(),
				                                    smResp.getRetrievalEndTime() );
			}

			final Object countValueObj = countValueNode.getLiteralValue();

			final Integer cardinality;
			if ( countValueObj instanceof Integer value ) {
				cardinality = value.intValue();
			}
			else if ( countValueObj instanceof Long value ) {
				if ( Integer.MAX_VALUE < value )
					cardinality = Integer.MAX_VALUE;
				else
					cardinality = (int) value.longValue();
			}
			else {
				final String msg = "The result obtained for a cardinality-related SPARQL query does not have an integer or long literal for the count variable (but " + countValueObj.getClass() + ").";
				return new CardinalityResponseImpl( new IllegalArgumentException(msg),
				                                    smResp.getRequestStartTime(),
				                                    smResp.getRetrievalEndTime() );
			}

			// Finally, we can create the cardinality response.
			return new CardinalityResponseImpl( cardinality,
			                                    smResp.getRequestStartTime(),
			                                    smResp.getRetrievalEndTime() );
		}
	}

	protected static class FunctionToObtainCardinalityResponseFromTPFResponse implements Function<TPFResponse, CardinalityResponse>
	{
		public CardinalityResponse apply( final TPFResponse tpfResp ) {
			// First, check that the given response is not defective
			// and does not represent an error response.
			if ( tpfResp.isError() )
				return new CardinalityResponseImpl( tpfResp.getErrorStatusCode(),
				                                    tpfResp.getErrorDescription(),
				                                    tpfResp.getRequestStartTime(),
				                                    tpfResp.getRetrievalEndTime() );

			if ( tpfResp.isDefective() )
				return new CardinalityResponseImpl( tpfResp.getException(),
				                                    tpfResp.getRequestStartTime(),
				                                    tpfResp.getRetrievalEndTime() );

			// Now, we extract the cardinality from
			// the solution mapping of the response.

			final Integer cardinality;
			try {
				cardinality = tpfResp.getCardinalityEstimate();
			}
			catch ( final UnsupportedOperationDueToRetrievalError e ) {
				throw new IllegalStateException("We should not end up here.", e);
			}

			if ( cardinality == null ) {
				final String msg = "Cardinality estimation for a TPF or brTPF request is unavailable due to missing metadata triples.";
				return new CardinalityResponseImpl( new IllegalArgumentException(msg),
				                                    tpfResp.getRequestStartTime(),
				                                    tpfResp.getRetrievalEndTime() );
			}


			return new CardinalityResponseImpl( cardinality,
			                                    tpfResp.getRequestStartTime(),
			                                    tpfResp.getRetrievalEndTime() );
		}
	}

}
