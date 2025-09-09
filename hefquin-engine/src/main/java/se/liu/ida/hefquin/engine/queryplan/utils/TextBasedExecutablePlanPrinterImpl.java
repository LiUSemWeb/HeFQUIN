package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.List;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.StatsOfPushBasedExecutablePlan;

public class TextBasedExecutablePlanPrinterImpl extends BaseForTextBasedPlanPrinters
		implements ExecutablePlanPrinter
{	
	protected String parentIndent = "";

	@Override
	public void print( final ExecutablePlan plan, final PrintStream out ) {
		if ( plan instanceof PushBasedExecutablePlanImpl p ) {
			print(p, out);
		}
		else {
			throw new IllegalArgumentException("Unsupported type of executable plan (" + plan.getClass().getName() + ").");
		}
	}

	public void print( final PushBasedExecutablePlanImpl plan, final PrintStream out ) {
		final StatsOfPushBasedExecutablePlan planStats = (StatsOfPushBasedExecutablePlan) plan.getStats();
		@SuppressWarnings("unchecked")
		final List<Stats> statsOfTasks = (List<Stats>) planStats.getEntry("statsOfTasks");

		int i = 0;
		for(final Stats statsOfTask : statsOfTasks){
			final ExecutableOperatorStats opStats = (ExecutableOperatorStats) statsOfTask.getEntry("operatorStats");
			print(opStats, i, out);
			i++;
		}
	}
	
	public void print( final ExecutableOperatorStats stats,
	                   final int planLevel,
	                   final PrintStream out ) {
		final String indentLevelString = getIndentLevelString(0, planLevel, 1, parentIndent);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(0, planLevel, 1, 0, indentLevelString);

		// Print the first line prefixed with indentLevelString,
		// prefix remaining line with indentLevelStringForOpDetail
		boolean first = true;
		for ( String line : stats.getShortString().split( "\\R", -1 ) ) {
			if ( first ) {
				out.print(indentLevelString);
				out.print(line);
				out.print(System.lineSeparator());
				first = false;
			} else {
				out.print(indentLevelStringForOpDetail);
				out.print(line);
				out.print(System.lineSeparator());
			}
		}

		parentIndent = indentLevelString;
	}
}
