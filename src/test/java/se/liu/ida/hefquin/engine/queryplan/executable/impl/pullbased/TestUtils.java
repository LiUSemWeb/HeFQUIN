package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.impl.optimizer.CostModel;

public class TestUtils extends EngineTestBase
{
	public static ExecutionContext createExecContextForTests() {
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest ();
		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public CostModel getCostModel() { return null; }
			@Override public boolean isExperimentRun() { return true; }
		};
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
	}

}
