package se.liu.ida.hefquin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedSolutionMapping;
import se.liu.ida.hefquin.data.jenaimpl.JenaBasedTriple;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BRTPFInterface;
import se.liu.ida.hefquin.federation.access.BRTPFRequest;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFInterface;
import se.liu.ida.hefquin.federation.access.TPFRequest;
import se.liu.ida.hefquin.federation.access.TPFResponse;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.impl.iface.BRTPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TPFResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TriplesResponseImpl;
import se.liu.ida.hefquin.federation.catalog.impl.FederationCatalogImpl;
import se.liu.ida.hefquin.query.jenaimpl.JenaBasedTriplePattern;

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

		public TriplesResponse performRequest( final TriplePatternRequest req ) {
			final List<Triple> result = getMatchingTriples(req);
			return new TriplesResponseImpl( result, this, req, new Date() );
		}

		protected List<Triple> getMatchingTriples( final TriplePatternRequest req ) {
			return getMatchingTriples( (JenaBasedTriplePattern) req.getQueryPattern() );
		}

		protected List<Triple> getMatchingTriples( final JenaBasedTriplePattern tp ) {
			final org.apache.jena.graph.Triple jenaTP = tp.asTriple();
			final Iterator<org.apache.jena.graph.Triple> it = data.find(jenaTP);
			final List<Triple> result = new ArrayList<>();
			while ( it.hasNext() ) {
				result.add( new JenaBasedTriple(it.next()) );
			}
			return result;
		}
	}

	protected static class SPARQLEndpointForTest implements SPARQLEndpoint
	{
		final SPARQLEndpointInterface iface;

		public SPARQLEndpointForTest() { this("http://example.org/sparql"); }

		public SPARQLEndpointForTest( final String ifaceURL ) {
			iface = new SPARQLEndpointInterfaceImpl(ifaceURL);
		}

		@Override
		public SPARQLEndpointInterface getInterface() { return iface; }
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
			final org.apache.jena.graph.Triple jenaTP = ( (JenaBasedTriplePattern) req.getTriplePattern() ).asTriple();

			final List<org.apache.jena.graph.Triple> patternsForTest = new ArrayList<>();
			for ( final SolutionMapping sm : req.getSolutionMappings() ) {
				final Binding b = ((JenaBasedSolutionMapping) sm).asJenaBinding();
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
						result.add( new JenaBasedTriple(t) );
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
			return new SolMapsResponseImpl( itSolMapsForResponse.next(), fm, req, new Date() );
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
