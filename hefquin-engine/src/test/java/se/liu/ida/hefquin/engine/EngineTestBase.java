package se.liu.ida.hefquin.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

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
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUESorFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashRJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverterImpl;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverterImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.CardinalityResponse;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalResponse;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.FederationAccessStats;
import se.liu.ida.hefquin.federation.access.Neo4jRequest;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.RecordsResponse;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.StringResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.federation.members.BRTPFServer;
import se.liu.ida.hefquin.federation.members.Neo4jServer;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;
import se.liu.ida.hefquin.federation.members.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.members.TPFServer;
import se.liu.ida.hefquin.federation.members.impl.BaseForFederationMember;
import se.liu.ida.hefquin.federation.members.impl.TPFServerImpl;

public abstract class EngineTestBase
{
	/**
	 * Change this flag to true if you also want to run the
	 * unit tests that access servers on the actual Web.
	 */
	public static boolean skipLiveWebTests = true;

	protected LogicalToPhysicalOpConverter getLOP2POPForTests() {
		return new LogicalToPhysicalOpConverterImpl( List.of(
				PhysicalOpBinaryUnion.getFactory(),
				PhysicalOpMultiwayUnion.getFactory(),
				PhysicalOpBind.getFactory(),
				PhysicalOpFilter.getFactory(),
				PhysicalOpRequest.getFactory(),
				PhysicalOpGlobalToLocal.getFactory(),
				PhysicalOpLocalToGlobal.getFactory(),
				PhysicalOpBindJoinBRTPF.getFactory(),
				PhysicalOpBindJoinWithBoundJoin.getFactory(),
				PhysicalOpBindJoinWithVALUESorFILTER.getFactory(),
				//PhysicalOpBindJoinWithUNION.getFactory(),
				//PhysicalOpBindJoinWithFILTER.getFactory(),
				//PhysicalOpBindJoinWithVALUES.getFactory(),
				PhysicalOpSymmetricHashJoin.getFactory(),
				PhysicalOpHashRJoin.getFactory(),
				PhysicalOpIndexNestedLoopsJoin.getFactory(),
				//PhysicalOpHashJoin.getFactory(),
				PhysicalOpNaiveNestedLoopsJoin.getFactory()
			)
		);
	}

	protected ExecutionContext getExecContextForTests( final ExecutorService execService ) {
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();
		final LogicalToPhysicalPlanConverter lp2pp = new LogicalToPhysicalPlanConverterImpl(false, false);
		final LogicalToPhysicalOpConverter lop2pop = getLOP2POPForTests();

		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { throw new UnsupportedOperationException(); }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return execService; }
			@Override public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { return lp2pp; }
			@Override public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() { return lop2pop; }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
		};
	}

	protected TPFServer getDBpediaTPFServer() {
		return new TPFServerImpl( "http://fragments.dbpedia.org/2016-04/en",
		                          null ); // no vocab.mapping
	}

	protected static abstract class FederationMemberBaseForTest extends BaseForFederationMember
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

	public static class SPARQLEndpointForTest extends FederationMemberBaseForTest implements SPARQLEndpoint
	{
		protected final String url;

		public SPARQLEndpointForTest() { this("http://example.org/sparql", null); }

		public SPARQLEndpointForTest( final String url ) { this(url, null); }

		public SPARQLEndpointForTest( final Graph data ) { this("http://example.org/sparql", data); }

		public SPARQLEndpointForTest( final String url, final Graph data ) {
			super(data);
			this.url  = url;
		}

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }

		@Override
		public String getURL() { return url; }

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

	}

	protected static class SPARQLEndpointWithVocabularyMappingForTest extends SPARQLEndpointForTest
	{
		final VocabularyMapping vm;

		public SPARQLEndpointWithVocabularyMappingForTest( final String ifaceURL, final Graph data , final VocabularyMapping vm) {
			super(ifaceURL, data);
			this.vm = vm;
		}

		@Override
		public VocabularyMapping getVocabularyMapping() { return vm; }
	}

	public static class TPFServerForTest extends FederationMemberBaseForTest implements TPFServer
	{
		public TPFServerForTest() { this(null); }
		public TPFServerForTest( final Graph data ) { super(data); }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }

		@Override
		public String getBaseURL() { return "http://example.org/"; }

		@Override
		public String createRequestURL( final TPFRequest req ) { throw new UnsupportedOperationException(); }

		public TPFResponse performRequest( final TPFRequest req ) {
			final List<Triple> result = getMatchingTriples(req);
			return new TPFResponseForTest(result, this, req);
		}
	}

	public static class BRTPFServerForTest extends FederationMemberBaseForTest implements BRTPFServer
	{
		public BRTPFServerForTest() { this(null); }
		public BRTPFServerForTest( final Graph data ) { super(data); }

		@Override
		public VocabularyMapping getVocabularyMapping() { return null; }

		@Override
		public String getBaseURL() { return "http://example.org/"; }

		@Override
		public String createRequestURL( final TPFRequest req ) { throw new UnsupportedOperationException(); }

		@Override
		public String createRequestURL( final BRTPFRequest req ) { throw new UnsupportedOperationException(); }

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
	}

	protected static class BRTPFServerWithVocabularyMappingForTest extends BRTPFServerForTest
	{
		final VocabularyMapping vm;

		public BRTPFServerWithVocabularyMappingForTest(Graph data, VocabularyMapping vocabularyMapping) {
			super(data);
			vm = vocabularyMapping;
		}

		@Override
		public VocabularyMapping getVocabularyMapping() { return vm; }
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
		public < ReqType extends DataRetrievalRequest,
		         RespType extends DataRetrievalResponse<?>,
		         MemberType extends FederationMember >
		CompletableFuture<RespType> issueRequest( final ReqType req,
		                                          final MemberType fm )
				throws FederationAccessException
		{
			if ( req instanceof SPARQLRequest reqSPARQL && fm instanceof SPARQLEndpoint fmSPARQL ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) _issueRequest(reqSPARQL, fmSPARQL);
				return resp;
			}

			if ( req instanceof BRTPFRequest reqBRTPF && fm instanceof BRTPFServer fmBRTPF ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) _issueRequest(reqBRTPF, fmBRTPF);
				return resp;
			}

			if ( req instanceof TPFRequest reqTPF && fm instanceof BRTPFServer fmBRTPF ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) _issueRequest(reqTPF, fmBRTPF);
				return resp;
			}

			if ( req instanceof TPFRequest reqTPF && fm instanceof TPFServer fmTPF && !(fm instanceof BRTPFServer)) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) _issueRequest(reqTPF, fmTPF);
				return resp;
			}

			if ( req instanceof Neo4jRequest reqNeo && fm instanceof Neo4jServer fmNeo ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) _issueRequest(reqNeo, fmNeo);
				return resp;
			}

			if ( req instanceof RESTRequest reqREST && fm instanceof RESTEndpoint ep ) {
				@SuppressWarnings("unchecked")
				final CompletableFuture<RespType> resp = (CompletableFuture<RespType>) _issueRequest(reqREST, ep);
				return resp;
			}

			throw new UnsupportedOperationException();
		}

		protected CompletableFuture<SolMapsResponse> _issueRequest( final SPARQLRequest req,
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

		protected CompletableFuture<TPFResponse> _issueRequest( final TPFRequest req,
		                                                        final TPFServer fm ) {
			final TPFResponse response;
			if ( itTriplesForResponse != null ) {
				response = new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				response = ( (TPFServerForTest) fm ).performRequest(req);
			}
			return CompletableFuture.completedFuture(response);
		}

		protected CompletableFuture<TPFResponse> _issueRequest( final TPFRequest req,
		                                                        final BRTPFServer fm ) {
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

		protected CompletableFuture<TPFResponse> _issueRequest( final BRTPFRequest req,
		                                                        final BRTPFServer fm ) {
			final TPFResponse response;
			if ( itTriplesForResponse != null ) {
				response = new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				response = ( (BRTPFServerForTest) fm ).performRequest(req);
			}
			return CompletableFuture.completedFuture(response);
		}

		protected CompletableFuture<RecordsResponse> _issueRequest( final Neo4jRequest req,
		                                                            final Neo4jServer fm )
				throws FederationAccessException
		{
			final Neo4jRequestProcessor reqProc = new Neo4jRequestProcessorImpl();
			final RecordsResponse response = reqProc.performRequest(req, fm);
			return CompletableFuture.completedFuture(response);
		}

		protected CompletableFuture<StringResponse> _issueRequest( final RESTRequest req,
		                                                           final RESTEndpoint fm )
						throws FederationAccessException
		{
			final String data = "{ \"current\": { \"temperature_2m\": 2.3, \"wind_speed_10m\": 1.0 } }";
			final StringResponse response = new StringResponseImpl(data, fm, req, new Date());
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
		public void resetStats() { throw new UnsupportedOperationException(); }

		@Override
		public FederationAccessStats getStats() { throw new UnsupportedOperationException(); }

		@Override
		public void shutdown() { } // do nothing
	}

}
