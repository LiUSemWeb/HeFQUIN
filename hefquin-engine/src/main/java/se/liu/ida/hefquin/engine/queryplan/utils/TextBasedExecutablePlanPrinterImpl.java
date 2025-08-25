package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanStats;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBind;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithFILTER;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithUNION;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithVALUES;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpBindJoinSPARQLwithVALUESorFILTER;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpFilter;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpHashRJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpIndexNestedLoopsJoinTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpParallelMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestBRTPF;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestSPARQL;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatBRTPFServer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpRequestTPFatTPFServer;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.ExecOpSymmetricHashJoin;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedPlanThread;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.StatsOfPushBasedExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.StatsOfPushBasedPlanThread;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class TextBasedExecutablePlanPrinterImpl extends BaseForTextBasedPlanPrinters
		implements ExecutablePlanPrinter
{	
	protected String parentIndent = "";

	@Override
	public void print( final ExecutablePlan plan, final PrintStream out ) {
		if ( plan instanceof PushBasedExecutablePlanImpl p ) {
			print(p, out);
		}
	}

	public void print( final PushBasedExecutablePlanImpl plan, final PrintStream out ) {
		final StatsOfPushBasedExecutablePlan planStats = (StatsOfPushBasedExecutablePlan) plan.getStats();
		@SuppressWarnings("unchecked")
		List<Stats> statsOfTasks = (List<Stats>) planStats.getEntry("statsOfTasks");

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
