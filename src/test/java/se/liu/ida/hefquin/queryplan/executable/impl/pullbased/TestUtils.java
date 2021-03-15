package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import se.liu.ida.hefquin.federation.FederationAccessManager;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.federation.access.SolMapsResponse;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class TestUtils
{
	public static ExecutionContext createExecContextForTests() {
		return new ExecutionContext( new FederationAccessManagerTestImpl() );
	}

	public static ResultElementIterator<String> createResultElementIteratorForTests( final String[] elements ) {
		return new ResultElementIteratorForTests<String>( elements );
	}

	public static ResultBlockIterator<String> createResultBlockIteratorForTests( final String[] elements, final int blockSize ) {
		return new ResultBlockIterOverResultElementIter<String>(
				createResultElementIteratorForTests(elements),
				new GenericIntermediateResultBlockBuilderImpl<String>(),
				blockSize );
	}


	public static class ResultElementIteratorForTests<ElmtType> implements ResultElementIterator<ElmtType>
	{
		final protected Iterator<ElmtType> it;

		public ResultElementIteratorForTests( final ElmtType[] elements ) {
			this( Arrays.asList(elements) );
		}

		public ResultElementIteratorForTests( final List<ElmtType> list ) {
			it = list.iterator();
		}

		@Override
		public boolean hasNext() { return it.hasNext(); }

		@Override
		public ElmtType next() { return it.next(); }
	}

	protected static class FederationAccessManagerTestImpl implements FederationAccessManager
	{
		@Override
		public SolMapsResponse performRequest( final SPARQLRequest req, final SPARQLEndpoint fm ) {
			return null;
		}
	}
}
