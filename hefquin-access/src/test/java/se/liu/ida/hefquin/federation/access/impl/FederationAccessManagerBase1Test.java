package se.liu.ida.hefquin.federation.access.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.federation.FederationTestBase;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.UnsupportedOperationDueToRetrievalError;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;

public class FederationAccessManagerBase1Test extends FederationTestBase
{
	protected static boolean PRINT_TIME = false;
	protected static final long SLEEP_MILLIES = 0L;
	//protected static boolean PRINT_TIME = true;
	//protected static final long SLEEP_MILLIES = 100L;

	@Test
	public void performCardinalityRequestSPARQL()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final SPARQLRequest req = new SPARQLRequestImpl( tp );
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card );

		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();

		assertEquals( fm, r.getFederationMember() );
		assertEquals( card, r.getCardinality() );
	}

	@Test
	public void performCardinalityRequestTPF()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req = new TPFRequestImpl( tp );
		final TPFServer fm = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card );

		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();

		assertEquals( fm, r.getFederationMember() );
		assertEquals( card, r.getCardinality() );
	}

	@Test
	public void twoCardinalityRequestsInParallel()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl( tp );
		final TPFRequest req2 = new TPFRequestImpl( tp );
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card );

		final long startTime = new Date().getTime();

		final CompletableFuture<CardinalityResponse> fr1 = fedAccessMgr.issueCardinalityRequest( req1, fm1 );
		final CompletableFuture<CardinalityResponse> fr2 = fedAccessMgr.issueCardinalityRequest( req2, fm2 );

		final CardinalityResponse r1 = fr1.get();
		final CardinalityResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) {
			System.out.println( "twoCardinalityRequestsInParallel \t milliseconds passed: " + (endTime - startTime) );
		}

		assertEquals( fm1, r1.getFederationMember() );
		assertEquals( fm2, r2.getFederationMember() );
		assertEquals( card, r1.getCardinality() );
		assertEquals( card, r2.getCardinality() );
	}

	@Test
	public void twoCardinalityRequestsInSequence()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req1 = new TPFRequestImpl( tp );
		final TPFRequest req2 = new TPFRequestImpl( tp );
		final TPFServer fm1 = new TPFServerForTest();
		final TPFServer fm2 = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card );

		final long startTime = new Date().getTime();

		final CompletableFuture<CardinalityResponse> fr1 = fedAccessMgr.issueCardinalityRequest( req1, fm1 );
		final CardinalityResponse r1 = fr1.get();

		final CompletableFuture<CardinalityResponse> fr2 = fedAccessMgr.issueCardinalityRequest( req2, fm2 );
		final CardinalityResponse r2 = fr2.get();

		final long endTime = new Date().getTime();
		if ( PRINT_TIME ) {
			System.out.println( "twoCardinalityRequestsInSequence \t milliseconds passed: " + (endTime - startTime) );
		}

		assertEquals( fm1, r1.getFederationMember() );
		assertEquals( fm2, r2.getFederationMember() );
		assertEquals( card, r1.getCardinality() );
		assertEquals( card, r2.getCardinality() );
	}

	@Test
	public void performCardinalityRequestSPARQLWithErrorResponse()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final SPARQLRequest req = new SPARQLRequestImpl( tp );
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card, true );
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		assertTrue( r.isError() );
		assertThrows( UnsupportedOperationDueToRetrievalError.class, () -> r.getCardinality() );
		assertThrows( UnsupportedOperationDueToRetrievalError.class, () -> r.getResponseData() );
	}

	@Test
	public void performSPARQLRequestWithErrorResponse()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final SPARQLRequest req = new SPARQLRequestImpl( tp );
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card, true );
		final CompletableFuture<SolMapsResponse> ftr = fedAccessMgr.issueRequest( req, fm );
		final SolMapsResponse r = ftr.get();
		assertTrue( r.isError() );
		assertThrows( UnsupportedOperationDueToRetrievalError.class, () -> r.getResponseData() );
	}

	@Test
	public void performCardinalityRequestTPFWithErrorResponse()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req = new TPFRequestImpl( tp );
		final TPFServer fm = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card, true );
		final CardinalityResponse r = fedAccessMgr.issueCardinalityRequest( req, fm ).get();
		assertTrue( r.isError() );
		assertThrows( UnsupportedOperationDueToRetrievalError.class, () -> r.getCardinality() );
		assertThrows( UnsupportedOperationDueToRetrievalError.class, () -> r.getResponseData() );
	}

	@Test
	public void performTPFRequestTPFWithErrorResponse()
			throws FederationAccessException, InterruptedException, ExecutionException
	{
		final TriplePattern tp = new TriplePatternImpl( NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode(),
		                                                NodeFactory.createBlankNode() );
		final TPFRequest req = new TPFRequestImpl( tp );
		final TPFServer fm = new TPFServerForTest();

		final int card = 42;
		final FederationAccessManager fedAccessMgr = createMyFedAccessMgr( card, true );
		final CompletableFuture<TPFResponse> ftr = fedAccessMgr.issueRequest( req, fm );
		final TPFResponse r = ftr.get();
		assertTrue( r.isError() );
		assertThrows( UnsupportedOperationDueToRetrievalError.class, () -> r.getResponseData() );
	}

	// ------------ helper code ------------

	protected FederationAccessManager createMyFedAccessMgr( final int card ) {
		return createMyFedAccessMgr( card, false );
	}

	protected FederationAccessManager createMyFedAccessMgr( final int card, final boolean simulateError ) {
		return new MyFederationAccessManagerForTests( Integer.valueOf( card ), SLEEP_MILLIES, simulateError );
	}

	protected class MyFederationAccessManagerForTests extends FederationAccessManagerBase1
	{
		protected final Integer card;
		protected final long sleepMillis;
		protected final boolean simulateError;

		public MyFederationAccessManagerForTests( final Integer card, final long sleepMillis, final boolean simulateError ) {
			this.card = card;
			this.sleepMillis = sleepMillis;
			this.simulateError = simulateError;
		}

		@Override
		public <ReqType extends DataRetrievalRequest,
		        RespType extends DataRetrievalResponse<?>,
		        MemberType extends FederationMember>
		CompletableFuture<RespType> issueRequest( final ReqType req, final MemberType fm)
				throws FederationAccessException
		{
			if (    req instanceof SPARQLRequest reqSPARQL
			     && fm instanceof SPARQLEndpoint fmSPARQL ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) create( reqSPARQL, fmSPARQL );
				return resp;
			}

			if ( req instanceof TPFRequest || req instanceof BRTPFRequest ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) createTPFResponse( fm, req );
				return resp;
			}

			throw new UnsupportedOperationException();
		}

		protected CompletableFuture<SolMapsResponse> create( final SPARQLRequest req,
		                                                     final SPARQLEndpoint fm ) {
			final Node countNode = NodeFactory.createLiteralByValue( card, XSDDatatype.XSDint );
			final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping( countVar, countNode );
			final SolMapsResponse r;
			if ( ! simulateError ) {
				r = new SolMapsResponseImpl( Arrays.asList(sm), fm, req, new Date() );
			} else {
				r = new SolMapsResponseImpl( Arrays.asList(sm), fm, req, new Date(), 400, "Response error" );
			}

			return CompletableFuture.supplyAsync( () -> {
				if ( sleepMillis > 0L ) {
					try {
						Thread.sleep( sleepMillis );
					} catch ( final InterruptedException e ) {
						throw new RuntimeException( e );
					}
				}

				return r;
			});
		}

		protected CompletableFuture<TPFResponse> createTPFResponse( final FederationMember fm,
		                                                            final DataRetrievalRequest req )
		{
			final TPFResponse r;
			if ( ! simulateError ) {
				r = new TPFResponseImpl( Collections.emptyList(),
				                         Collections.emptyList(),
				                         null,
				                         fm,
				                         req,
				                         new Date() ) {
					@Override
					public Integer getCardinalityEstimate() {
						return card;
					};
				};
			}
			else {
				r = new TPFResponseImpl( Collections.emptyList(),
				                         Collections.emptyList(),
				                         null,
				                         fm,
				                         req,
				                         new Date(),
				                         400,
				                         "Response error" );
			}

			return CompletableFuture.supplyAsync( () -> {
				if ( sleepMillis > 0L ) {
					try {
						Thread.sleep( sleepMillis );
					} catch ( final InterruptedException e ) {
						throw new RuntimeException( e );
					}
				}

				return r;
			});
		}

		@Override
		protected void _resetStats() {
			// nothing to do here
		}

		@Override
		protected FederationAccessStatsImpl _getStats() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void shutdown() {
			// do nothing
		}
	}
}
