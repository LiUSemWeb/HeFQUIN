package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class TestUtils extends EngineTestBase
{
	public static ExecutionContext createExecContextForTests() {
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest ();
		return new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
		};
	}

	public static SolutionMapping createSolutionMappingForTests() {
		return new SolutionMappingForTests(null);
	}

	public static SolutionMapping createSolutionMappingForTests( final String token ) {
		return new SolutionMappingForTests(token);
	}

	public static ResultElementIterator createResultElementIteratorForTests( final SolutionMapping... elements ) {
		return new ResultElementIteratorForTests( elements );
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
