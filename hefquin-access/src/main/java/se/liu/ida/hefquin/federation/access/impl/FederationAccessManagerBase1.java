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
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.CardinalityEstimationUnavailableError;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
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
import se.liu.ida.hefquin.federation.access.impl.response.CardinalityResponseImplWithoutCardinality;

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

	protected AtomicLong issuedCardRequestsSPARQL = new AtomicLong( 0L );
	protected AtomicLong issuedCardRequestsTPF    = new AtomicLong( 0L );
	protected AtomicLong issuedCardRequestsBRTPF  = new AtomicLong( 0L );

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
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
		// (it needs to be a COUNT(*) without DISTINCT,
		//  and we need a variable for it)
		final Expr countExpr = countQuery.allocAggregate( AggregatorFactory.createCount( false ) );
		countQuery.addResultVar( countVar, countExpr );

		// issue the query as a request, the response will then be processed to create
		// the CardinalityResponse to be returned
		final SPARQLRequest reqCount = new SPARQLRequestImpl( new SPARQLQueryImpl( countQuery ) );
		return issueRequest( reqCount, fm ).thenApply( getFctToObtainCardinalityResponseFromSolMapsResponse() );
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
			final TPFRequest req,
			final TPFServer fm )
					throws FederationAccessException
	{
		return issueRequest(req, fm).thenApply( getFctToObtainCardinalityResponseFromTPFResponse() );
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
			final TPFRequest req,
			final BRTPFServer fm )
					throws FederationAccessException
	{
		return issueRequest(req, fm).thenApply( getFctToObtainCardinalityResponseFromTPFResponse() );
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
			final BRTPFRequest req,
			final BRTPFServer fm )
					throws FederationAccessException
	{
		return issueRequest(req, fm).thenApply( getFctToObtainCardinalityResponseFromTPFResponse() );
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

	protected Function<SolMapsResponse, CardinalityResponse> getFctToObtainCardinalityResponseFromSolMapsResponse() {
		return new FunctionToObtainCardinalityResponseFromSolMapsResponse();
	}

	protected Function<TPFResponse, CardinalityResponse> getFctToObtainCardinalityResponseFromTPFResponse() {
		return new FunctionToObtainCardinalityResponseFromTPFResponse();
	}

	// ---------- HELPER CLASSES ----------

	protected static class FunctionToObtainCardinalityResponseFromSolMapsResponse implements Function<SolMapsResponse, CardinalityResponse>
	{
		public CardinalityResponse apply( final SolMapsResponse smResp ) {
			final Integer cardinality;
			try {
				cardinality = extractCardinality( smResp );
			}
			catch ( final UnsupportedOperationDueToRetrievalError | IllegalArgumentException e ) {
				return new CardinalityResponseImplWithoutCardinality( e, smResp, smResp.getRequest() );
			}

			return new CardinalityResponseImpl( smResp, smResp.getRequest(), cardinality );
		}

		protected Integer extractCardinality( final SolMapsResponse smResp ) throws UnsupportedOperationDueToRetrievalError {
			final Iterator<SolutionMapping> it = smResp.getResponseData().iterator();
			final SolutionMapping sm = it.next();
			final Node countValueNode = sm.asJenaBinding().get( countVar );
			final Object countValueObj = countValueNode.getLiteralValue();

			if ( countValueObj instanceof Integer value ) {
				return value.intValue();
			}
			else if ( countValueObj instanceof Long value ) {
				return (Integer.MAX_VALUE < value) ? Integer.MAX_VALUE : (int) value.longValue();
			}
			else {
				throw new IllegalArgumentException( "Expected literal to be of type Integer or Long but was " + countValueObj.getClass() );
			}
		}
	}

	protected static class FunctionToObtainCardinalityResponseFromTPFResponse implements Function<TPFResponse, CardinalityResponse>
	{
		public CardinalityResponse apply( final TPFResponse tpfResp ) {
			if ( tpfResp == null ) {
				throw new IllegalArgumentException( "The given TPFResponse is null" );
			}

			final Integer cardinality = tpfResp.getCardinalityEstimate();
			if ( cardinality != null ) {
				final int c = cardinality;
				return new CardinalityResponseImpl( tpfResp, tpfResp.getRequest(), c );
			}
			else {
				final CardinalityEstimationUnavailableError e = new CardinalityEstimationUnavailableError(
					"Cardinality estimation is unavailable due to missing metadata triples.",
					tpfResp.getRequest(),
					tpfResp.getFederationMember()
				);
				return new CardinalityResponseImplWithoutCardinality( e, tpfResp, tpfResp.getRequest() );
			}
		}
	}

}
