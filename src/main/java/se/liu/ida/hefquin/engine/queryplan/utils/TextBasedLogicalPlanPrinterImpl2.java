package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;

public class TextBasedLogicalPlanPrinterImpl2 implements LogicalPlanPrinter
{
	// The string represents '|  '.
	private static String levelIndentBase = "\u2502  ";
	// The string represents '├──'.
	private static String nonLastChildIndentBase = "\u251C\u2500\u2500";
	// The string represents '└──'.
	private static String lastChildIndentBase = "\u2514\u2500\u2500";
		
	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
		planWalk(plan, 0, 0, 0, out);
		out.flush();	
	}
	
	public String getIndentLevelString(final int planNumber, final int planLevel, final int numberOfSiblings) {
		String indentLevelString = "";
		for ( int i = 1; i < planLevel; i++ ) {
			indentLevelString += levelIndentBase;
		}
		if (planNumber < numberOfSiblings-1) {
			indentLevelString += nonLastChildIndentBase ;
		}
		else if (numberOfSiblings > 0) {
			indentLevelString += lastChildIndentBase;
		}
		else {
			// This is only for the root operator.
			indentLevelString = "";
		}
		return indentLevelString;
	}
	
	/**
	 * This method recursively goes through a plan, and appends specific strings to a print stream.
	 * @param plan The current plan (root operator) that will be formatted.
	 * @param planNumber The number of a plan in terms of its super plan.
	 * @param planLevel The depth of the root operator in a plan.
	 * @param numberOfSiblings The number of sibling plans of a plan.
	 * @param out The print stream that will print a plan.
	 */
	public void planWalk( final LogicalPlan plan, final int planNumber, final int planLevel, final int numberOfSiblings, final PrintStream out) {
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings);
		if (planNumber < numberOfSiblings-1) {
			out.append( indentLevelString + plan.getRootOperator().toString() );
		}
		else {
			out.append( indentLevelString + plan.getRootOperator().toString() );
		}
		out.append( System.lineSeparator() );
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out );
		}
	}

}
