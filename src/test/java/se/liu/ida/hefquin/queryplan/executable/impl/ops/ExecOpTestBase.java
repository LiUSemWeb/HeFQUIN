package se.liu.ida.hefquin.queryplan.executable.impl.ops;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.data.Triple;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.SPARQLEndpointInterface;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TPFInterface;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.federation.access.impl.iface.SPARQLEndpointInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.iface.TPFInterfaceImpl;
import se.liu.ida.hefquin.federation.access.impl.response.SolMapsResponseImpl;
import se.liu.ida.hefquin.federation.access.impl.response.TriplesResponseImpl;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;

public abstract class ExecOpTestBase
{
	protected static class SPARQLEndpointForTest implements SPARQLEndpoint
	{
		final SPARQLEndpointInterface iface = new SPARQLEndpointInterfaceImpl();

		@Override
		public SPARQLEndpointInterface getInterface() { return iface; }
	}

	protected static class TPFServerForTest implements TPFServer
	{
		final TPFInterface iface = new TPFInterfaceImpl();

		@Override
		public TPFInterface getInterface() { return iface; }
	}

	protected static class IntermediateResultElementSinkForTest implements IntermediateResultElementSink
	{
		protected final List<SolutionMapping> l = new ArrayList<>();

		@Override
		public void send(SolutionMapping element) {
			l.add(element);
		}

		public Iterator<SolutionMapping> getSolMapsIter() {
			return l.iterator();
		}
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

		@Override
		public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
			return new SolMapsResponseImpl(itSolMapsForResponse.next(), fm);
		}

		@Override
		public TriplesResponse performRequest( final TriplePatternRequest req, final TPFServer fm ) {
			return new TriplesResponseImpl(itTriplesForResponse.next(), fm);
		}

		@Override
		public TriplesResponse performRequest( final TriplePatternRequest req, final BRTPFServer fm ) {
			return null;
		}

		@Override
		public TriplesResponse performRequest( final BindingsRestrictedTriplePatternRequest req, final BRTPFServer fm ) {
			return null;
		}
	}

}
