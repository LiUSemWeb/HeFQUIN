package se.liu.ida.hefquin.engine.queryproc.impl;

import static org.junit.Assert.*;

import org.junit.Test;

import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryproc.ExecutionStats;
import se.liu.ida.hefquin.engine.queryproc.QueryPlanningStats;
import se.liu.ida.hefquin.engine.queryproc.impl.execution.ExecutionStatsImpl;

public class QueryProcessingStatsAndExceptionsImplTest
{
	@Test
	public void test() {
		final QueryPlanningStats ps = null;
		final ExecutablePlanStats planStats = null;
		final ExecutionStats es = new ExecutionStatsImpl(planStats);

		final QueryProcessingStatsAndExceptionsImpl s = new QueryProcessingStatsAndExceptionsImpl(4L, 1L, 1L, 2L, ps, es);
		s.put( "additionalEntry", Integer.valueOf(42) );

		// check that the entries are in the correct order
		String lastEntryName = null;
		for ( final String n : s.getEntryNames() ) {
			lastEntryName = n;
		}
		assertEquals( "additionalEntry", lastEntryName );

		// check the correctness of the entries
		assertEquals( 4L, s.getOverallQueryProcessingTime() );
		assertEquals( 1L, s.getPlanningTime() );
		assertEquals( 1L, s.getCompilationTime() );
		assertEquals( 2L, s.getExecutionTime() );

		assertEquals( ps, s.getQueryPlanningStats() );
		assertEquals( es, s.getExecutionStats() );

		assertEquals( 42, s.getEntry("additionalEntry") );
	}

}
