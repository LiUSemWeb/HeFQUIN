package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.utils.IndentingPrintStream;

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
		final IndentingPrintStream iOut = new IndentingPrintStream(out);
		planWalk(plan, 0, 0, 0, iOut);
		iOut.flush();	
	}
	
	public String getIndentLevelString(final int planNumber, final int planLevel, final int numberOfSiblings) {
		String indentLevelString = "";
		for ( int i = 1; i < planLevel; i++ ) {
			indentLevelString += levelIndentBase;
		}
		if (planNumber < numberOfSiblings-1) {
			indentLevelString += nonLastChildIndentBase ;
		}
		else {
			if (numberOfSiblings > 0) {
				indentLevelString += lastChildIndentBase;
			}
			else {
				indentLevelString = "";
			}
		}
		return indentLevelString;
	}
	
	
	public void planWalk( final LogicalPlan plan, final int planNumber, final int planLevel, final int numberOfSiblings, final IndentingPrintStream out) {
		String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings);
		if (planNumber < numberOfSiblings-1) {
			//out.append( plan.getRootOperator().printString(indentLevelString,   levelIndentBase) );
			out.append( indentLevelString + plan.getRootOperator().toString() );
		}
		else {
			//out.append( plan.getRootOperator().printString(indentLevelString,   levelIndentBase) );
			out.append( indentLevelString + plan.getRootOperator().toString() );
		}
		out.append( System.lineSeparator() );
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out );
		}
	}

}
