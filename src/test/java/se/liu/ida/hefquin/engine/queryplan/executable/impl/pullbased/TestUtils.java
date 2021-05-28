package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import org.apache.jena.sparql.engine.binding.Binding;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.queryplan.ExecutableOperator;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

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

		@Override
		public Binding asJenaBinding() { throw new UnsupportedOperationException(); }
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

		@Override
		public ExecutableOperator getOp() {
			return null;
		}

		@Override
		public int getArity() {
			return 0;
		}

		@Override
		public ResultElementIterator getSubIterator(final int i) throws NoSuchElementException {
			return null;
		}
	}

	protected static class FederationAccessManagerTestImpl implements FederationAccessManager
	{
		@Override
		public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
			return null;
		}

		@Override
		public TPFResponse performRequest( final TPFRequest req, final TPFServer fm ) {
			return null;
		}

		@Override
		public TPFResponse performRequest( final TPFRequest req, final BRTPFServer fm ) {
			return null;
		}

		@Override
		public TPFResponse performRequest( final BRTPFRequest req, final BRTPFServer fm ) {
			return null;
		}
	}
}
