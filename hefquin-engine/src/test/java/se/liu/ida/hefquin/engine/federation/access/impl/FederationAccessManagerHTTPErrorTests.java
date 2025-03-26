package se.liu.ida.hefquin.engine.federation.access.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpResponseException;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.impl.TripleImpl;
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
import se.liu.ida.hefquin.engine.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.BRTPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.SPARQLRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TPFRequestImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;

public class FederationAccessManagerHTTPErrorTests extends EngineTestBase
{
	protected static ExecutorService execServiceForFedAccess;
	final TriplePattern tp = new TriplePatternImpl( NodeFactory.createVariable( "s" ),
	                                                NodeFactory.createVariable( "p" ),
	                                                NodeFactory.createVariable( "o" ) );
	final SPARQLRequest sparqlReq = new SPARQLRequestImpl( tp );
	final TPFRequest tpfReq = new TPFRequestImpl( tp );
	final BRTPFRequest brtpfReq = new BRTPFRequestImpl( tp, Collections.emptySet() );

	@BeforeClass
	public static void createExecService() {
		final int numberOfThreads = 10;
		execServiceForFedAccess = Executors.newFixedThreadPool( numberOfThreads );
	}

	@AfterClass
	public static void tearDownExecService() {
		execServiceForFedAccess.shutdownNow();
		try {
			execServiceForFedAccess.awaitTermination( 500L, TimeUnit.MILLISECONDS );
		} catch ( final InterruptedException ex ) {
			System.err.println( "Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}
	}

	// SPARQL request tests

	@Test
	public void sparqlCardinalityNoErrorTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests();
		final int card1 = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get().getCardinality();
		final int card2 = fedAccessMgr.issueRequest( sparqlReq, fm ).get().getSize();
		assertEquals( card1, card2 );
	}

	@Test
	public void sparqlCardinalityHTTPExceptionTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final SPARQLEndpoint fm = new SPARQLEndpointForTest();
		for( final int errorCode : new int[]{ 400, 403, 404, 408, 415, 428, 500, 502, 503, 504 }){
			final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests( errorCode );
			final CardinalityResponse response = fedAccessMgr.issueCardinalityRequest( sparqlReq, fm ).get();
			validateErrorResponse( response, errorCode );
		}
	}

	@Test
	public void tpfCardinalityNoErrorTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final TPFServer fm = new TPFServerForTest();
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests();
		final int card1 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get().getCardinality();
		final int card2 = fedAccessMgr.issueRequest( tpfReq, fm ).get().getSize();
		assertEquals( card1, card2 );
	}

	@Test
	public void tpfCardinalityHTTPExceptionTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final TPFServer fm = new TPFServerForTest();
		for( final int errorCode : new int[]{ 400, 403, 404, 408, 415, 428, 500, 502, 503, 504 }){
			final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests( errorCode );
			final CardinalityResponse response = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
			validateErrorResponse( response, errorCode );
		}
	}

	@Test
	public void tpfWithBrtpfServerCardinalityNoErrorTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final BRTPFServer fm = new BRTPFServerForTest();
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests();
		final int card1 = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get().getCardinality();
		final int card2 = fedAccessMgr.issueRequest( tpfReq, fm ).get().getSize();
		assertEquals( card1, card2 );
	}

	@Test
	public void tpfWithBrtpfServerCardinalityHTTPExceptionTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final BRTPFServer fm = new BRTPFServerForTest();
		for( final int errorCode : new int[]{ 400, 403, 404, 408, 415, 428, 500, 502, 503, 504 }){
			final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests( errorCode );
			final CardinalityResponse response = fedAccessMgr.issueCardinalityRequest( tpfReq, fm ).get();
			validateErrorResponse( response, errorCode );
		}
	}

	@Test
	public void brtpfCardinalityNoErrorTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final BRTPFServer fm = new BRTPFServerForTest();
		final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests();
		final int card1 = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get().getCardinality();
		final int card2 = fedAccessMgr.issueRequest( brtpfReq, fm ).get().getSize();
		assertEquals( card1, card2 );
	}

	@Test
	public void brtpfCardinalityHTTPExceptionTest() throws FederationAccessException, IOException, InterruptedException, ExecutionException {
		final BRTPFServer fm = new BRTPFServerForTest();
		for( final int errorCode : new int[]{ 400, 403, 404, 408, 415, 428, 500, 502, 503, 504 }){
			final FederationAccessManager fedAccessMgr = createFedAccessMgrForTests( errorCode );
			final CardinalityResponse response = fedAccessMgr.issueCardinalityRequest( brtpfReq, fm ).get();
			validateErrorResponse( response, errorCode );
		}
	}

	// ------------ helper code ------------

	public static void validateErrorResponse( final CardinalityResponse response, final Integer errorCode ){
		assertTrue( response.isError() );
		assertEquals( errorCode, response.getErrorStatusCode() );
		try {
			response.getResponseData();
			fail( "Expected UnsupportedOperationDueToRetrievalError" );
		} catch ( UnsupportedOperationDueToRetrievalError e ) {
			// Expected exception; do nothing
		} catch ( Exception e ) {
			fail( "Unexpected exception type: " + e.getClass().getName() );
		}
		final int cardinality = response.getCardinality();
		assertEquals( Integer.MAX_VALUE, cardinality );
	}

	public static FederationAccessManager createFedAccessMgrForTests() throws IOException {
		return createFedAccessMgrForTests( -1 );
	}

	public static FederationAccessManager createFedAccessMgrForTests( final int errorCode ) throws IOException {
		final SPARQLRequestProcessor reqProc = new MySPARQLRequestProcessor( errorCode );
		final TPFRequestProcessor reqProcTPF = new MyTPFRequestProcessor( errorCode );
		final BRTPFRequestProcessor reqProcBRTPF = new MyBRTPFRequestProcessor( errorCode );
		final Neo4jRequestProcessor reqProcNeo4j = new Neo4jRequestProcessor() {
			@Override
			public RecordsResponse performRequest( Neo4jRequest req, Neo4jServer fm ) {
				return null;
			}
		};

		return new AsyncFederationAccessManagerImpl( execServiceForFedAccess,
		                                             reqProc,
		                                             reqProcTPF,
		                                             reqProcBRTPF,
		                                             reqProcNeo4j );
	}

	protected static class MyTPFServerForTest implements TPFServer
	{
		protected final TPFInterface iface;
		protected final long errorCode;

		public MyTPFServerForTest( final int errorCode ) {
			this.errorCode = errorCode;
			iface = new TPFInterfaceImpl( "http://example.org/tpf", "subject", "predicate", "object" );
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}

		@Override
		public TPFInterface getInterface() {
			return iface;
		}
	}

	protected static class MyBRTPFServerForTest implements BRTPFServer
	{
		protected final long errorCode;
		protected final BRTPFInterface iface;

		public MyBRTPFServerForTest( final int errorCode ) {
			this.errorCode = errorCode;
			iface = new BRTPFInterfaceImpl( "http://example.org/brtpf", "subject", "predicate", "object", "values" );
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}

		@Override
		public BRTPFInterface getInterface() {
			return iface;
		}
	}

	protected static class MySPARQLRequestProcessor extends SPARQLRequestProcessorImpl
	{
		protected final int errorCode;

		public MySPARQLRequestProcessor( final int errorCode ) {
			this.errorCode = errorCode;
		}

		@Override
		public SolMapsResponse performRequest( SPARQLRequest req, SPARQLEndpoint fm ) throws FederationAccessException {
			final FederationAccessException e = getException( req, fm, errorCode );
			if( e != null) throw e;

			final List<SolutionMapping> solMaps = new ArrayList<>();
			// vars
			final Var s = Var.alloc( "s" );
			final Var o = Var.alloc( "o" );
			final Var p = Var.alloc( "p" );
			for( final Triple triple : getTriples()){
				solMaps.add( SolutionMappingUtils.createSolutionMapping( s, triple.asJenaTriple().getSubject(),
				                                                         p, triple.asJenaTriple().getPredicate(),
				                                                         o, triple.asJenaTriple().getObject() ) );
			}

			final List<SolutionMapping> result = new ArrayList<>();

			final Var var = Var.alloc( "__hefquinCountVar" );
			if ( req.getExpectedVariables().getPossibleVariables().contains( var ) ) {
				// add cardinality solution mapping
				final Node countLiteral = NodeFactory.createLiteralByValue( solMaps.size(), XSDDatatype.XSDinteger );
				final SolutionMapping solMap = SolutionMappingUtils.createSolutionMapping( var, countLiteral );
				result.add( solMap );
			} else {
				// or all solution mappings
				result.addAll(solMaps);
			}
			return new SolMapsResponseImpl( result, fm, req, new Date() );
		}
	}

	protected static class MyTPFRequestProcessor extends TPFRequestProcessorImpl
	{
		protected final int errorCode;

		public MyTPFRequestProcessor( final int errorCode ) {
			this.errorCode = errorCode;
		}

		@Override
		public TPFResponse performRequest( TPFRequest req, TPFServer fm ) throws FederationAccessException {
			final FederationAccessException e = getException( req, fm, errorCode );
			if( e != null) throw e;

			final TPFResponse r = new TPFResponseImpl( getTriples(),
			                                           Collections.emptyList(),
			                                           null,
			                                           fm,
			                                           req,
			                                           new Date() ) {
				@Override
				public Integer getCardinalityEstimate() {
					return matchingTriples.size();
				};
			};
			return r;
		}

		@Override
		public TPFResponse performRequest( TPFRequest req, BRTPFServer fm ) throws FederationAccessException {
			final FederationAccessException e = getException( req, fm, errorCode );
			if( e != null) throw e;

			final TPFResponse r = new TPFResponseImpl( getTriples(),
			                                           Collections.emptyList(),
			                                           null,
			                                           fm,
			                                           req,
			                                           new Date() ) {
				@Override
				public Integer getCardinalityEstimate() {
					return matchingTriples.size();
				};
			};
			return r;
		}
	}

	protected static class MyBRTPFRequestProcessor extends BRTPFRequestProcessorImpl
	{
		protected final int errorCode;

		public MyBRTPFRequestProcessor( final int errorCode ) {
			this.errorCode = errorCode;
		}

		@Override
		public TPFResponse performRequest( BRTPFRequest req, BRTPFServer fm ) throws FederationAccessException {
			final FederationAccessException e = getException( req, fm, errorCode );
			if( e != null) throw e;

			final TPFResponse r = new TPFResponseImpl( Collections.emptyList(), Collections.emptyList(), null, fm, req,
					new Date() ) {
				@Override
				public Integer getCardinalityEstimate() {
					return matchingTriples.size();
				};
			};
			return r;
		}
	}

	protected static List<Triple> getTriples() {
		// values
		final Node sVal = NodeFactory.createURI( "http://example.org/s" );
		final Node pVal = NodeFactory.createURI( "http://example.org/p" );
		final Node o1Val = NodeFactory.createURI( "http://example.org/o1" );
		final Node o2Val = NodeFactory.createURI( "http://example.org/o2" );
		final Node o3Val = NodeFactory.createURI( "http://example.org/o3" );

		final List<Triple> triples = new ArrayList<>();
		triples.add( new TripleImpl( sVal, pVal, o1Val ) );
		triples.add( new TripleImpl( sVal, pVal, o2Val ) );
		triples.add( new TripleImpl( sVal, pVal, o3Val ) );
		return triples;
	}

	protected static FederationAccessException getException( final DataRetrievalRequest req,
	                                                         final FederationMember fm,
	                                                         final int errorCode ){
		final HttpResponseException e;
		switch ( errorCode ) {
			case 400 -> e = new HttpResponseException( 400, "Bad Request" );
			case 403 -> e = new HttpResponseException( 403, "Forbidden" );
			case 404 -> e = new HttpResponseException( 404, "Not Found" );
			case 408 -> e = new HttpResponseException( 408, "Request Timeout" );
			case 415 -> e = new HttpResponseException( 415, "Unsupported Media Type" );
			case 428 -> e = new HttpResponseException( 428, "Too Many Requests" );
			case 500 -> e = new HttpResponseException( 500, "Internal Server Error" );
			case 502 -> e = new HttpResponseException( 502, "Bad Gateway" );
			case 503 -> e = new HttpResponseException( 503, "Service Unavailable" );
			case 504 -> e = new HttpResponseException( 504, "Gateway Timeout" );
			default -> e = null;
		}
		
		if ( e != null )
			return new FederationAccessException( e, req, fm );
		
		return null;
	}
}