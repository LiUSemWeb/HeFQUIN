package se.liu.ida.hefquin.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingBuilder;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.Triple;
import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.base.data.impl.TripleImpl;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.engine.federation.*;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.Neo4jInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;

public abstract class EngineTestBase
{
	/**
	 * Change this flag to true if you also want to run the
	 * unit tests that access servers on the actual Web.
	 */
	public static boolean skipLiveWebTests = true;

	/**
	 * If this flag is true, tests that make requests to local neo4j
	 * instances will be skipped.
	 */
	public static boolean skipLocalNeo4jTests = true;

	/**
	 * If true, skip tests to local GraphQL endpoint
	 */
	public static boolean skipLocalGraphQLTests = true;


	protected TPFServer getDBpediaTPFServer() {
		final String       tpfServerBaseURL = "http://fragments.dbpedia.org/2016-04/en";
		final TPFInterface tpfServerIface   = new TPFInterfaceImpl(tpfServerBaseURL, "subject", "predicate", "object");
		return new TPFServer() {
			@Override public TPFInterface getInterface() { return tpfServerIface; }

			@Override
			public VocabularyMapping getVocabularyMapping() {
				return null;
			}
		};
	}

	protected static abstract class FederationMemberBaseForTest implements FederationMember
	{
		protected final Graph data;

		public FederationMemberBaseForTest( final Graph data ) {
			this.data = data;
		}

		protected List<Triple> getMatchingTriples( final TriplePatternRequest req ) {
			return getMatchingTriples( req.getQueryPattern() );
		}

		protected List<Triple> getMatchingTriples( final TriplePattern tp ) {
			final org.apache.jena.graph.Triple jenaTP = tp.asJenaTriple();
			final Iterator<org.apache.jena.graph.Triple> it = data.find(jenaTP);
			final List<Triple> result = new ArrayList<>();
			while ( it.hasNext() ) {
				result.add( new TripleImpl(it.next()) );
			}
			return result;
		}

		protected List<SolutionMapping> getSolutions( final TriplePatternRequest req ) {
			return getSolutions( req.getQueryPattern() );
		}

		protected List<SolutionMapping> getSolutions( final TriplePattern tp ) {
			final List<Triple> triples = getMatchingTriples(tp);
			final List<SolutionMapping> result = new ArrayList<>();
			for ( final Triple t : triples ) {
				final BindingBuilder b = BindingBuilder.create();
				if ( tp.asJenaTriple().getSubject().isVariable() ) {
					b.add( Var.alloc(tp.asJenaTriple().getSubject()), t.asJenaTriple().getSubject() );
				}
				if ( tp.asJenaTriple().getPredicate().isVariable() ) {
					b.add( Var.alloc(tp.asJenaTriple().getPredicate()), t.asJenaTriple().getPredicate() );
				}
				if ( tp.asJenaTriple().getObject().isVariable() ) {
					b.add( Var.alloc(tp.asJenaTriple().getObject()), t.asJenaTriple().getObject() );
				}
				result.add( new SolutionMappingImpl(b.build()) );
			}
			return result;
		}
		
		protected List<SolutionMapping> getSolutions( final SPARQLGraphPattern pattern ) {
			final Op jenaOp;
			if ( pattern instanceof GenericSPARQLGraphPatternImpl1 ) {
				@SuppressWarnings("deprecation")
				final Op o = ( (GenericSPARQLGraphPatternImpl1) pattern ).asJenaOp();
				jenaOp = o;
			}
			else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 ) {
				jenaOp = ( (GenericSPARQLGraphPatternImpl2) pattern ).asJenaOp();
			}
			else {
				throw new UnsupportedOperationException( pattern.getClass().getName() );
			}

			final QueryIterator qIter = Algebra.exec(jenaOp, data);
			final List<SolutionMapping> results = new ArrayList<>();
			while ( qIter.hasNext() ){
				final Binding b = qIter.nextBinding() ;
				results.add(new SolutionMappingImpl(b));
			}
			return results;	
		}
		
	}

	protected static class SPARQLEndpointForTest extends FederationMemberBaseForTest implements SPARQLEndpoint
	{
		final SPARQLEndpointInterface iface;

		public SPARQLEndpointForTest() { this("http://example.org/sparql", null); }

		public SPARQLEndpointForTest( final String ifaceURL ) { this(ifaceURL, null); }

		public SPARQLEndpointForTest( final Graph data ) { this("http://example.org/sparql", data); }

		public SPARQLEndpointForTest( final String ifaceURL, final Graph data ) {
			super(data);
			iface = new SPARQLEndpointInterfaceImpl(ifaceURL);
		}

		@Override
		public SPARQLEndpointInterface getInterface() { return iface; }

		public SolMapsResponse performRequest( final SPARQLRequest req )
				throws FederationAccessException
		{
			final List<SolutionMapping> result;
			if ( req instanceof TriplePatternRequest ) {
				result = getSolutions( (TriplePatternRequest) req);
			}
			else if (req.getQueryPattern() instanceof TriplePattern) {
				result = getSolutions( (TriplePattern) req.getQueryPattern() );
			} else {
				result = getSolutions(req.getQueryPattern());
			}
			return new SolMapsResponseImpl( result, this, req, new Date() );
		}
		
		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}

	}

	protected static class SPARQLEndpointWithVocabularyMappingForTest extends FederationMemberBaseForTest implements SPARQLEndpoint
	{
		final SPARQLEndpointInterface iface;
		final VocabularyMapping vocabularyMapping;

		public SPARQLEndpointWithVocabularyMappingForTest( final String ifaceURL, final Graph data , final VocabularyMapping vm) {
			super(data);
			iface = new SPARQLEndpointInterfaceImpl(ifaceURL);
			vocabularyMapping = vm;
		}

		@Override
		public SPARQLEndpointInterface getInterface() { return iface; }

		public SolMapsResponse performRequest( final SPARQLRequest req ) {
			final List<SolutionMapping> result;
			if ( req instanceof TriplePatternRequest ) {
				result = getSolutions( (TriplePatternRequest) req);
			}
			else if (req.getQueryPattern() instanceof TriplePattern) {
				result = getSolutions( (TriplePattern) req.getQueryPattern() );
			} else {
				result = getSolutions(req.getQueryPattern());
			}
			return new SolMapsResponseImpl( result, this, req, new Date() );
		}
		
		@Override
		public VocabularyMapping getVocabularyMapping() {
			return vocabularyMapping;
		}

	}

	protected static class TPFServerForTest extends FederationMemberBaseForTest implements TPFServer
	{
		protected final TPFInterface iface = new TPFInterfaceImpl("http://example.org/", "subject", "predicate", "object");

		public TPFServerForTest() { this(null); }
		public TPFServerForTest( final Graph data ) { super(data); }

		@Override
		public TPFInterface getInterface() { return iface; }

		public TPFResponse performRequest( final TPFRequest req ) {
			final List<Triple> result = getMatchingTriples(req);
			return new TPFResponseForTest(result, this, req);
		}
		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}
	}


	protected static class BRTPFServerForTest extends FederationMemberBaseForTest implements BRTPFServer
	{
		final BRTPFInterface iface = new BRTPFInterfaceImpl("http://example.org/", "subject", "predicate", "object", "values");

		public BRTPFServerForTest() { this(null); }
		public BRTPFServerForTest( final Graph data ) { super(data); }

		@Override
		public BRTPFInterface getInterface() { return iface; }

		public TPFResponse performRequest( final TPFRequest req ) {
			final List<Triple> result = getMatchingTriples(req);
			return new TPFResponseForTest(result, this, req);
		}

		public TPFResponse performRequest( final BindingsRestrictedTriplePatternRequest req ) {
			// The implementation in this method is not particularly efficient,
			// but it is sufficient for the purpose of unit tests.
			final org.apache.jena.graph.Triple jenaTP = req.getTriplePattern().asJenaTriple();

			final List<org.apache.jena.graph.Triple> patternsForTest = new ArrayList<>();
			for ( final Binding sm : req.getSolutionMappings() ) {
				final Node s = ( jenaTP.getSubject().isVariable() )
						? sm.get( Var.alloc(jenaTP.getSubject()) ) // may be null
						: null;
				final Node p = ( jenaTP.getPredicate().isVariable() )
						? sm.get( Var.alloc(jenaTP.getPredicate()) ) // may be null
						: null;
				final Node o = ( jenaTP.getObject().isVariable() )
						? sm.get( Var.alloc(jenaTP.getObject()) ) // may be null
						: null;
				patternsForTest.add( org.apache.jena.graph.Triple.createMatch(s,p,o) );
			}

			final Iterator<org.apache.jena.graph.Triple> it = data.find(jenaTP);
			final List<Triple> result = new ArrayList<>();
			while ( it.hasNext() ) {
				final org.apache.jena.graph.Triple t = it.next();
				for ( final org.apache.jena.graph.Triple patternForTest : patternsForTest ) {
					if ( patternForTest.matches(t) ) {
						result.add( new TripleImpl(t) );
						break;
					}
				}
			}
			return new TPFResponseForTest(result, this, req);
		}
		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}
	}
	
	protected static class BRTPFServerWithVocabularyMappingForTest extends FederationMemberBaseForTest implements BRTPFServer
	{
		final VocabularyMapping vm;
		
		public BRTPFServerWithVocabularyMappingForTest(Graph data, VocabularyMapping vocabularyMapping) {
			super(data);
			vm = vocabularyMapping;
		}
		final BRTPFInterface iface = new BRTPFInterfaceImpl("http://example.org/", "subject", "predicate", "object", "values");

		@Override
		public BRTPFInterface getInterface() { return iface; }

		public TPFResponse performRequest( final TPFRequest req ) {
			final List<Triple> result = getMatchingTriples(req);
			return new TPFResponseForTest(result, this, req);
		}
		
		public TPFResponse performRequest( final BindingsRestrictedTriplePatternRequest req ) {
			// The implementation in this method is not particularly efficient,
			// but it is sufficient for the purpose of unit tests.
			final org.apache.jena.graph.Triple jenaTP = req.getTriplePattern().asJenaTriple();

			final List<org.apache.jena.graph.Triple> patternsForTest = new ArrayList<>();
			for ( final Binding sm : req.getSolutionMappings() ) {
				final Node s = ( jenaTP.getSubject().isVariable() )
						? sm.get( Var.alloc(jenaTP.getSubject()) ) // may be null
						: null;
				final Node p = ( jenaTP.getPredicate().isVariable() )
						? sm.get( Var.alloc(jenaTP.getPredicate()) ) // may be null
						: null;
				final Node o = ( jenaTP.getObject().isVariable() )
						? sm.get( Var.alloc(jenaTP.getObject()) ) // may be null
						: null;
				patternsForTest.add( org.apache.jena.graph.Triple.createMatch(s,p,o) );
			}

			final Iterator<org.apache.jena.graph.Triple> it = data.find(jenaTP);
			final List<Triple> result = new ArrayList<>();
			while ( it.hasNext() ) {
				final org.apache.jena.graph.Triple t = it.next();
				for ( final org.apache.jena.graph.Triple patternForTest : patternsForTest ) {
					if ( patternForTest.matches(t) ) {
						result.add( new TripleImpl(t) );
						break;
					}
				}
			}
			return new TPFResponseForTest(result, this, req);
		}
		@Override
		public VocabularyMapping getVocabularyMapping() {
			return vm;
		}
	}

	protected static class Neo4jServerImpl4Test implements Neo4jServer {

		public Neo4jServerImpl4Test() {}

		@Override
		public Neo4jInterface getInterface() {
			return new Neo4jInterfaceImpl("http://localhost:7474/db/neo4j/tx");
		}

		@Override
		public VocabularyMapping getVocabularyMapping() {
			return null;
		}
	}

	protected static class TPFResponseForTest extends TPFResponseImpl
	{
		public TPFResponseForTest( final List<Triple> matchingTriples,
		                           final FederationMember fm,
		                           final DataRetrievalRequest req ) {
			super( matchingTriples, new ArrayList<Triple>(), null, fm, req, new Date() );
		}

		@Override
		public Boolean isLastPage() { return true; }
	}


	protected static class FederationAccessManagerForTest implements FederationAccessManager
	{
		protected final Iterator<List<SolutionMapping>> itSolMapsForResponse;
		protected final Iterator<List<Triple>> itTriplesForResponse;

		public FederationAccessManagerForTest( final Iterator<List<SolutionMapping>> itSolMapsForResponses,
											   final Iterator<List<Triple>> itTriplesForResponses )
		{
			this.itSolMapsForResponse = itSolMapsForResponses;
			this.itTriplesForResponse = itTriplesForResponses;
		}

		public FederationAccessManagerForTest( final List<SolutionMapping> solMapsForResponse,
											   final List<Triple> triplesForResponse )
		{
			this( (solMapsForResponse != null) ? Arrays.asList(solMapsForResponse).iterator() : null,
					(triplesForResponse != null) ? Arrays.asList(triplesForResponse).iterator() : null );
		}

		public FederationAccessManagerForTest()
		{
			this.itSolMapsForResponse = null;
			this.itTriplesForResponse = null;
		}

		@Override
		public CompletableFuture<SolMapsResponse> issueRequest(
				final SPARQLRequest req,
				final SPARQLEndpoint fm )
						throws FederationAccessException
		{
			final SolMapsResponse response;
			if ( itSolMapsForResponse != null ) {
				response = new SolMapsResponseImpl( itSolMapsForResponse.next(), fm, req, new Date() );
			}
			else {
				if (fm.getVocabularyMapping() != null) {
					response = ( (SPARQLEndpointWithVocabularyMappingForTest) fm ).performRequest(req);
				} else {
					response = ( (SPARQLEndpointForTest) fm ).performRequest(req);
				}
			}
			return CompletableFuture.completedFuture(response);
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req,
		                                                    final TPFServer fm )
				throws FederationAccessException
		{
			final TPFResponse response;
			if ( itTriplesForResponse != null ) {
				response = new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				response = ( (TPFServerForTest) fm ).performRequest(req);
			}
			return CompletableFuture.completedFuture(response);
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req,
		                                                    final BRTPFServer fm )
				throws FederationAccessException
		{
			final TPFResponse response;
			if ( itTriplesForResponse != null ) {
				response = new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				if (fm.getVocabularyMapping() != null) {
					response = ( (BRTPFServerWithVocabularyMappingForTest) fm).performRequest(req);
				} else {
					response = ( (BRTPFServerForTest) fm ).performRequest(req);
				}
			}
			return CompletableFuture.completedFuture(response);
		}

		@Override
		public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req,
		                                                    final BRTPFServer fm )
				throws FederationAccessException
		{
			final TPFResponse response;
			if ( itTriplesForResponse != null ) {
				response = new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				response = ( (BRTPFServerForTest) fm ).performRequest(req);
			}
			return CompletableFuture.completedFuture(response);
		}

		@Override
		public CompletableFuture<RecordsResponse> issueRequest( final Neo4jRequest req,
		                                                       final Neo4jServer fm )
				throws FederationAccessException
		{
			final Neo4jRequestProcessor reqProc = new Neo4jRequestProcessorImpl();
			final RecordsResponse response = reqProc.performRequest(req, fm);
			return CompletableFuture.completedFuture(response);
		}

		@Override
		public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
				final SPARQLRequest req,
				final SPARQLEndpoint fm ) throws FederationAccessException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
				final TPFRequest req,
				final TPFServer fm ) throws FederationAccessException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
				final TPFRequest req,
				final BRTPFServer fm ) throws FederationAccessException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public CompletableFuture<CardinalityResponse> issueCardinalityRequest(
				final BRTPFRequest req,
				final BRTPFServer fm ) throws FederationAccessException
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public void resetStats() {
			throw new UnsupportedOperationException();
		}

		@Override
		public FederationAccessStats getStats() {
			throw new UnsupportedOperationException();
		}

		@Override
		public void shutdown() {
			// do nothing
		}
	}
}
