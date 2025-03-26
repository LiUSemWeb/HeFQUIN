package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

import org.apache.http.client.HttpResponseException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.aggregate.AggregatorFactory;
import org.roaringbitmap.longlong.IntegerUtil;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.QueryPatternUtils;
import se.liu.ida.hefquin.base.query.impl.SPARQLQueryImpl;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.CardinalityResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.CardinalityResponseImplWithoutCardinality;

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

	protected AtomicLong issuedCardRequestsSPARQL  = new AtomicLong(0L);
	protected AtomicLong issuedCardRequestsTPF     = new AtomicLong(0L);
	protected AtomicLong issuedCardRequestsBRTPF   = new AtomicLong(0L);

	/**
	 * Checks whether the given {@link Throwable} or any of its causes is an instance of the specified exception type.
	 *
	 * @param throwable  the exception to inspect (may be null)
	 * @param targetType the class of the exception type to search for
	 * @return {@code true} if any cause matches {@code targetType}
	 */
	public static boolean hasCause( final Throwable throwable, final Class<? extends Throwable> targetType ) {
		return getCause( throwable, targetType ) != null;
	}

	/**
	 * Returns the underlying cause of the given {@link Throwable} that is an instance of the specified exception type,
	 * or {@code null} if no such cause is found.
	 *
	 * @param throwable  the throwable to inspect
	 * @param targetType the target exception type to search for
	 * @return the cause of the specified type, or {@code null} if not found
	 */
	public static Throwable getCause( final Throwable throwable, final Class<? extends Throwable> targetType ) {
		for ( Throwable cause = throwable; cause != null; cause = cause.getCause() ) {
			if ( targetType.isInstance( cause ) )
				return cause;
		}
		return null;
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final SPARQLRequest req, final SPARQLEndpoint fm )
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
		countQuery.addResultVar(countVar, countExpr);

		// issue the query as a request, the response will then be processed to create
		// the CardinalityResponse to be returned
		final SPARQLRequest reqCount = new SPARQLRequestImpl( new SPARQLQueryImpl(countQuery) );

		// Keep track of when the request was issued in case an exception is thrown
		final Date requestStartTime = new Date();

		return issueRequest( reqCount, fm ).handle( ( result, throwable ) -> {
			if ( throwable != null ) {
				// if not caused by a (possibly wrapped) FederationException, re-throw
				if ( ! hasCause( throwable, FederationAccessException.class ) )
					throw new CompletionException( throwable );

				// otherwise use fallback
				final Node countValueNode = NodeFactory.createLiteralByValue( Integer.MAX_VALUE, XSDDatatype.XSDint );
				final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping( countVar, countValueNode );

				// TODO: Is HttpResponseException the only relevant cause?
				final HttpResponseException cause = (HttpResponseException) getCause( throwable, HttpResponseException.class );
				final SolMapsResponse fallback = new SolMapsResponseImpl( List.of( sm ),
				                                                          fm,
				                                                          req,
				                                                          requestStartTime,
				                                                          cause != null ? cause.getStatusCode() : 500,
				                                                          cause != null ? cause.getReasonPhrase() : throwable.getMessage() );
				return getFctToObtainCardinalityResponseFromSolMapsResponse().apply( fallback );
			}
			return getFctToObtainCardinalityResponseFromSolMapsResponse().apply( result );
		} );
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req, final TPFServer fm )
		throws FederationAccessException
	{
		// Keep track of when the request was issued in case an exception is thrown
		final Date requestStartTime = new Date();

		return issueRequest( req, fm ).handle( ( result, throwable ) -> {
			if ( throwable != null ) {
				// if not caused by a (possibly wrapped) FederationException, re-throw
				if ( ! hasCause( throwable, FederationAccessException.class ) )
					throw new CompletionException( throwable );

				// TODO: Is HttpResponseException the only relevant cause?
				final HttpResponseException cause = (HttpResponseException) getCause( throwable, HttpResponseException.class );

				// otherwise use fallback (i.e., Integer.MAX_VALUE)
				final TPFResponse fallback = new TPFResponseImpl( Collections.emptyList(),
				                                                  Collections.emptyList(),
				                                                  null,
				                                                  Integer.MAX_VALUE,
				                                                  fm,
				                                                  req,
				                                                  requestStartTime,
				                                                  cause != null ? cause.getStatusCode() : 500,
				                                                  cause != null ? cause.getReasonPhrase() : throwable.getMessage() );
				return getFctToObtainCardinalityResponseFromTPFResponse().apply( fallback );
			}
			return getFctToObtainCardinalityResponseFromTPFResponse().apply( result );
		} );
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final TPFRequest req, final BRTPFServer fm )
		throws FederationAccessException
	{
		// Keep track of when the request was issued in case an exception is thrown
		final Date requestStartTime = new Date();

		return issueRequest( req, fm ).handle( ( result, throwable ) -> {
			if ( throwable != null ) {
				// if not caused by a (possibly wrapped) FederationException, re-throw
				if ( ! hasCause( throwable, FederationAccessException.class ) )
					throw new CompletionException( throwable );

				// TODO: Is HttpResponseException the only relevant cause?
				final HttpResponseException cause = (HttpResponseException) getCause( throwable, HttpResponseException.class );

				// otherwise use fallback (i.e., Integer.MAX_VALUE)
				final TPFResponse fallback = new TPFResponseImpl( Collections.emptyList(),
				                                                  Collections.emptyList(),
				                                                  null,
				                                                  Integer.MAX_VALUE,
				                                                  fm,
				                                                  req,
				                                                  requestStartTime,
				                                                  cause != null ? cause.getStatusCode() : 500,
				                                                  cause != null ? cause.getReasonPhrase() : throwable.getMessage() );
				return getFctToObtainCardinalityResponseFromTPFResponse().apply( fallback );
			}
			return getFctToObtainCardinalityResponseFromTPFResponse().apply( result );
		} );
	}

	@Override
	public CompletableFuture<CardinalityResponse> issueCardinalityRequest( final BRTPFRequest req, final BRTPFServer fm )
		throws FederationAccessException
	{
		return issueRequest( req, fm ).handle( ( result, throwable ) -> {
			if ( throwable != null ) {
				// if not caused by a (possibly wrapped) FederationException, re-throw
				if ( ! hasCause( throwable, FederationAccessException.class ) )
					throw new CompletionException( throwable );

				// TODO: Is HttpResponseException the only relevant cause?
				final HttpResponseException cause = (HttpResponseException) getCause( throwable, HttpResponseException.class );

				// otherwise use fallback (i.e., Integer.MAX_VALUE)
				final TPFResponse fallback = new TPFResponseImpl( Collections.emptyList(),
				                                                  Collections.emptyList(),
				                                                  null,
				                                                  Integer.MAX_VALUE,
				                                                  fm,
				                                                  req,
				                                                  new Date(),
				                                                  cause != null ? cause.getStatusCode() : 500,
				                                                  cause != null ? cause.getReasonPhrase() : throwable.getMessage() );
				return getFctToObtainCardinalityResponseFromTPFResponse().apply( fallback );
			}
			return getFctToObtainCardinalityResponseFromTPFResponse().apply( result );
		} );
	}

	@Override
	public final FederationAccessStats getStats() {
		final FederationAccessStatsImpl stats = _getStats();

		final long issuedCardRequestsSPARQL = this.issuedCardRequestsSPARQL.get();
		final long issuedCardRequestsTPF    = this.issuedCardRequestsTPF.get();
		final long issuedCardRequestsBRTPF  = this.issuedCardRequestsBRTPF.get();

		stats.put(enNumberOfSPARQLCardRequestsIssued, Long.valueOf(issuedCardRequestsSPARQL));
		stats.put(enNumberOfTPFCardRequestsIssued,    Long.valueOf(issuedCardRequestsTPF));
		stats.put(enNumberOfBRTPFCardRequestsIssued,  Long.valueOf(issuedCardRequestsBRTPF));

		final long overallCardRequests = issuedCardRequestsSPARQL
		                               + issuedCardRequestsTPF
		                               + issuedCardRequestsBRTPF;
		stats.put(enOverallNumberOfCardRequestsIssued, Long.valueOf(overallCardRequests));

		return stats;
	}


	@Override
	public final void resetStats() {
		issuedCardRequestsSPARQL.set(0L);
		issuedCardRequestsTPF.set(0L);
		issuedCardRequestsBRTPF.set(0L);

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
			final int cardinality = extractCardinality( smResp );
			if ( cardinality < Integer.MAX_VALUE ) {
				return new CardinalityResponseImpl( smResp, smResp.getRequest(), cardinality );
			}
			else {
				return new CardinalityResponseImplWithoutCardinality( smResp, smResp.getRequest() );
			}
		}

		protected Integer extractCardinality( final SolMapsResponse smResp ) {
			final Iterator<SolutionMapping> it = smResp.getSolutionMappings().iterator();
			final SolutionMapping sm = it.next();
			final Node countValueNode = sm.asJenaBinding().get( countVar );
			final Object countValueObj = countValueNode.getLiteralValue();

			if ( countValueObj instanceof Integer ) {
				return ((Integer) countValueObj).intValue();
			}
			else if ( countValueObj instanceof Long ) {
				final long l = ((Long) countValueObj).longValue();
				return (Integer.MAX_VALUE < l) ? Integer.MAX_VALUE : (int) l;
			}
			else {
				return Integer.MAX_VALUE;
			}
		}
	}

	protected static class FunctionToObtainCardinalityResponseFromTPFResponse implements Function<TPFResponse, CardinalityResponse>
	{
		public CardinalityResponse apply( final TPFResponse tpfResp ) {
			if ( tpfResp == null ) throw new IllegalArgumentException("The given TPFResponse is null");

			final Integer cardinality = tpfResp.getCardinalityEstimate();
			if ( cardinality != null && cardinality < Integer.MAX_VALUE ) {
				return new CardinalityResponseImpl(tpfResp, tpfResp.getRequest(), cardinality);
			}
			else {
				return new CardinalityResponseImplWithoutCardinality( tpfResp, tpfResp.getRequest() );
			}
		}
	}

}
