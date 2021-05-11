package se.liu.ida.hefquin.engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.algebra.Algebra;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.QueryIterator;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.engine.binding.BindingMap;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.impl.TripleImpl;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.engine.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.engine.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.engine.federation.access.TPFInterface;
import se.liu.ida.hefquin.engine.federation.access.TPFRequest;
import se.liu.ida.hefquin.engine.federation.access.TPFResponse;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.engine.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;

public abstract class EngineTestBase
{
	/**
	 * Change this flag to true if you also want to run the
	 * unit tests that access servers on the actual Web.
	 */
	public static boolean skipLiveWebTests = true;


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
				final BindingMap b = BindingFactory.create();
				if ( tp.asJenaTriple().getSubject().isVariable() ) {
					b.add( Var.alloc(tp.asJenaTriple().getSubject()), t.asJenaTriple().getSubject() );
				}
				if ( tp.asJenaTriple().getPredicate().isVariable() ) {
					b.add( Var.alloc(tp.asJenaTriple().getPredicate()), t.asJenaTriple().getPredicate() );
				}
				if ( tp.asJenaTriple().getObject().isVariable() ) {
					b.add( Var.alloc(tp.asJenaTriple().getObject()), t.asJenaTriple().getObject() );
				}
				result.add( new SolutionMappingImpl(b) );
			}
			return result;
		}
		
		protected List<SolutionMapping> getSolutions( final SPARQLGraphPattern pattern ) {
			final List<SolutionMapping> results = new ArrayList<>();
			final QueryIterator qIter = Algebra.exec(pattern.asJenaOp(), data);
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

	}


	protected static class TPFServerForTest extends FederationMemberBaseForTest implements TPFServer
	{
		protected final TPFInterface iface = new TPFInterfaceImpl();

		public TPFServerForTest() { this(null); }
		public TPFServerForTest( final Graph data ) { super(data); }

		@Override
		public TPFInterface getInterface() { return iface; }

		public TPFResponse performRequest( final TPFRequest req ) {
			final List<Triple> result = getMatchingTriples(req);
			return new TPFResponseForTest(result, this, req);
		}
	}


	protected static class BRTPFServerForTest extends FederationMemberBaseForTest implements BRTPFServer
	{
		final BRTPFInterface iface = new BRTPFInterfaceImpl();

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
			for ( final SolutionMapping sm : req.getSolutionMappings() ) {
				final Binding b = sm.asJenaBinding();
				final Node s = ( jenaTP.getSubject().isVariable() )
						? b.get( Var.alloc(jenaTP.getSubject()) ) // may be null
						: null;
				final Node p = ( jenaTP.getPredicate().isVariable() )
						? b.get( Var.alloc(jenaTP.getPredicate()) ) // may be null
						: null;
				final Node o = ( jenaTP.getObject().isVariable() )
						? b.get( Var.alloc(jenaTP.getObject()) ) // may be null
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


	protected static class TPFResponseForTest extends TPFResponseImpl
	{
		public TPFResponseForTest( final List<Triple> matchingTriples,
		                           final FederationMember fm,
		                           final DataRetrievalRequest req ) {
			super( matchingTriples, new ArrayList<Triple>(), fm, req, new Date() );
		}

		@Override
		public Boolean isLastPage() { return true; }
	}


	public static class FederationCatalogForTest extends FederationCatalogImpl
	{
		public void addMember( final String uri, final FederationMember fm ) {
			membersByURI.put(uri, fm);
		}
	} // end of FederationCatalogForTest


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
		public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
			if ( itSolMapsForResponse != null ) {
				return new SolMapsResponseImpl( itSolMapsForResponse.next(), fm, req, new Date() );
			}
			else {
				return ( (SPARQLEndpointForTest) fm ).performRequest(req);
			}
		}

		@Override
		public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) {
			if ( itTriplesForResponse != null ) {
				return new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				return ( (TPFServerForTest) fm ).performRequest(req);
			}
		}

		@Override
		public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) {
			if ( itTriplesForResponse != null ) {
				return new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				return ( (BRTPFServerForTest) fm ).performRequest(req);
			}
		}

		@Override
		public TPFResponse performRequest( final BRTPFRequest req, final BRTPFServer fm ) {
			if ( itTriplesForResponse != null ) {
				return new TPFResponseForTest( itTriplesForResponse.next(), fm, req );
			}
			else {
				return ( (BRTPFServerForTest) fm ).performRequest(req);
			}
		}
	}

}
