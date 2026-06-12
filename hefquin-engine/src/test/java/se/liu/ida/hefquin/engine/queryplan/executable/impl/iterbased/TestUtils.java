package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;

import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.utils.ExecutablePlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalOpConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.LogicalToPhysicalPlanConverter;
import se.liu.ida.hefquin.engine.queryplan.utils.PhysicalPlanPrinter;
import se.liu.ida.hefquin.engine.queryproc.QueryProcContextExt;
import se.liu.ida.hefquin.federation.access.*;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class TestUtils extends EngineTestBase
{
	public static QueryProcContextExt createQueryProcContextForTests() {
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();
		return new QueryProcContextExt() {
			@Override public FederationCatalog getFederationCatalog() { throw new UnsupportedOperationException(); }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { throw new UnsupportedOperationException(); }

			@Override public LogicalToPhysicalPlanConverter getLogicalToPhysicalPlanConverter() { throw new UnsupportedOperationException(); }
			@Override public LogicalToPhysicalOpConverter getLogicalToPhysicalOpConverter() { throw new UnsupportedOperationException(); }

			@Override public LogicalPlanPrinter getSourceAssignmentPrinter() { return null; }
			@Override public LogicalPlanPrinter getLogicalPlanPrinter() { return null; }
			@Override public PhysicalPlanPrinter getPhysicalPlanPrinter() { return null; }
			@Override public ExecutablePlanPrinter getExecutablePlanPrinter() { return null; }

			@Override public boolean isExperimentRun() { throw new UnsupportedOperationException(); }
			@Override public boolean skipExecution() { throw new UnsupportedOperationException(); }
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
