package se.liu.ida.hefquin.engine.federation.access.impl;

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
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;

public class FederationAccessManagerBase1Test extends EngineTestBase
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
		final SolMapsResponse r = fedAccessMgr.issueRequest( req, fm ).get();
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
		final TPFResponse r = fedAccessMgr.issueRequest( req, fm ).get();
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
		public CompletableFuture<SolMapsResponse> issueRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
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

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm ) {
			return createFutureTPFResponse( fm, req );
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm ) {
			return createFutureTPFResponse( fm, req );
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm ) {
			return createFutureTPFResponse( fm, req );
		}

		@Override
		public CompletableFuture<RecordsResponse> issueRequest( final Neo4jRequest req, final Neo4jServer fm ) {
			return null;
		}

		protected CompletableFuture<TPFResponse> createFutureTPFResponse( final FederationMember fm,
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
			// TODO Auto-generated method stub
			
		}

		@Override
		protected FederationAccessStatsImpl _getStats() {
			// TODO Auto-generated method stub
			return null;
		}
	}
}
