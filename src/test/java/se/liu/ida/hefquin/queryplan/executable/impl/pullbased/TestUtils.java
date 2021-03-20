package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.federation.BRTPFServer;
import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.TPFServer;
import se.liu.ida.hefquin.federation.access.BindingsRestrictedTriplePatternRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.TriplesResponse;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class TestUtils
{
	public static ExecutionContext createExecContextForTests() {
		return new ExecutionContext( new FederationAccessManagerTestImpl() );
	}

	public static SolutionMapping createSolutionMappingForTests() {
		return new SolutionMappingForTests(null);
	}

	public static SolutionMapping createSolutionMappingForTests( final String token ) {
		return new SolutionMappingForTests(token);
	}

	public static ResultElementIterator createResultElementIteratorForTests( final SolutionMapping[] elements ) {
		return new ResultElementIteratorForTests( elements );
	}

	public static ResultBlockIterator createResultBlockIteratorForTests( final int blockSize, final SolutionMapping... elements ) {
		return new ResultBlockIterOverResultElementIter(
				createResultElementIteratorForTests(elements),
				new GenericIntermediateResultBlockBuilderImpl(),
				blockSize );
	}


	public static class SolutionMappingForTests implements SolutionMapping
	{
		public final String token;

		public SolutionMappingForTests( final String token ) { this.token = token; }

		@Override
		public String toString() { return token; }
	}

	public static class ResultElementIteratorForTests implements ResultElementIterator
	{
		protected final Iterator<SolutionMapping> it;

		public ResultElementIteratorForTests( final SolutionMapping[] elements ) {
			this( Arrays.asList(elements) );
		}

		public ResultElementIteratorForTests( final List<SolutionMapping> list ) {
			it = list.iterator();
		}

		@Override
		public boolean hasNext() { return it.hasNext(); }

		@Override
		public SolutionMapping next() { return it.next(); }
	}

	protected static class FederationAccessManagerTestImpl implements FederationAccessManager
	{
		@Override
		public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
			return null;
		}

		@Override
		public TriplesResponse performRequest( final TriplePatternRequest req, final TPFServer fm ) {
			return null;
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
