package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.List;
import se.liu.ida.hefquin.base.utils.Stats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.PushBasedExecutablePlanImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pushbased.StatsOfPushBasedExecutablePlan;

public class TextBasedExecutablePlanPrinterImpl implements ExecutablePlanPrinter
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

	public void print( final PushBasedExecutablePlanImpl plan,
	                   final PrintStream out ) {
		print( (StatsOfPushBasedExecutablePlan) plan.getStats(), out );
	}

	public void print( final StatsOfPushBasedExecutablePlan planStats,
	                   final PrintStream out ) {
		if ( planStats.getEntry("statsOfTasks") instanceof List<?> l ) {
			@SuppressWarnings("unchecked")
			final List<Stats> statsOfTasks = (List<Stats>) l;
			print(statsOfTasks, out);
		}
		else {
			throw new IllegalArgumentException( planStats.getEntry("statsOfTasks").getClass().getName() );
		}
	}

	public void print( final List<Stats> statsOfTasks, final PrintStream out ) {
		int i = 0;
		for ( final Stats statsOfTask : statsOfTasks ) {
			final Object opStats = statsOfTask.getEntry("operatorStats");
			print( (ExecutableOperatorStats) opStats, i++, out );
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

	// The following was moved here from BaseForTextBasedPlanPrinters when
	// I reimplemented the printer for logical and for physical plans based
	// on PlanPrinter with PrintablePlan objects. I didn't bother do adapt
	// this class here as well, because it is somewhat useless.
	// By the way, in case the CLI does not print the executable plan even
	// with the --printExecutablePlan argument given, this may be the case
	// because the --skipExecution argument is given as well. The current
	// implementation of the QueryProcessor creates the executable plan
	// only when actually executing the query and, thus, that plan can be
	// printed only in this case.
	//                                     -Olaf

	// The string represents '|'.
	protected static String singleBase = "\u2502";
	// The string represents '|   '.
	protected static String levelIndentBase = "\u2502   ";
	// The string represents '├── '.
	protected static String nonLastChildIndentBase = "\u251C\u2500\u2500 ";
	// The string represents '└── '.
	protected static String lastChildIndentBase = "\u2514\u2500\u2500 ";
	protected static String spaceBase = "    ";

	protected String getIndentLevelString( final int planNumber,
	                                       final int planLevel,
	                                       final int numberOfSiblings,
	                                       final String upperRootOpIndentString ) {
		if ( planLevel == 0 ) {
			// This is only for the root operator of the overall plan to be printed.
			return "";
		}

		if ( upperRootOpIndentString.isEmpty() ) {
			if ( planNumber < numberOfSiblings-1 ) {
				return nonLastChildIndentBase;
			}
			else {
				return lastChildIndentBase;
			}
		}

		if ( upperRootOpIndentString.endsWith(nonLastChildIndentBase) ) {
			final String indentLevelString = upperRootOpIndentString.substring( 0, upperRootOpIndentString.length() - nonLastChildIndentBase.length() ) + levelIndentBase;

			if ( planNumber < numberOfSiblings-1 ) {
				return indentLevelString + nonLastChildIndentBase;
			}
			else {
				return indentLevelString + lastChildIndentBase;
			}
		}

		if ( upperRootOpIndentString.endsWith(lastChildIndentBase) ) {
			final String indentLevelString = upperRootOpIndentString.substring( 0, upperRootOpIndentString.length() - lastChildIndentBase.length() ) + spaceBase;
			if ( planNumber < numberOfSiblings-1 ) {
				return indentLevelString + nonLastChildIndentBase;
			}
			else {
				return indentLevelString + lastChildIndentBase;
			}
		}

		return "";
	}

	protected String getIndentLevelStringForDetail( final int planNumber,
	                                                final int planLevel,
	                                                final int numberOfSiblings,
	                                                final int numberOfSubPlans,
	                                                final String indentLevelString ) {
		if ( planLevel == 0 ) {
			if ( numberOfSubPlans > 0 ) {
				return "";
			}
			else {
				return spaceBase;
			}
		}

		if ( indentLevelString == "") {
			return spaceBase;
		}
		else if ( indentLevelString.endsWith(nonLastChildIndentBase) ) {
			return indentLevelString.substring( 0, indentLevelString.length() - nonLastChildIndentBase.length() ) + levelIndentBase;
		}
		else if ( indentLevelString.endsWith(lastChildIndentBase) ) {
			return indentLevelString.substring( 0, indentLevelString.length() - lastChildIndentBase.length() ) + spaceBase;
		}

		return "";
	}

}
