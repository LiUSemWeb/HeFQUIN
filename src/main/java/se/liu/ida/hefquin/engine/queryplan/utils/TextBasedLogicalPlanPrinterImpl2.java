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
	protected final String levelIndentBase = "\u2502  ";
	// The string represents '├──'.
	protected final String nonLastChildIndentBase = "\u251C\u2500\u2500";
	// The string represents '└──'.
	protected final String lastChildIndentBase = "\u2514\u2500\u2500";
		
	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
		final IndentingPrintStream iOut = new IndentingPrintStream(out);
		//final LogicalPlanVisitor beforeVisitor = new MyBeforeVisitor(iOut);
		//final LogicalPlanVisitor afterVisitor = new MyAfterVisitor(iOut);
		//LogicalPlanWalker.walk(plan, beforeVisitor, afterVisitor);
		//iOut.flush();
		
		planWalk(plan, 0, 0, 0, iOut);
		iOut.flush();
		
	}
	
	public String getIndentLevelString(int planNumber, int planLevel, int numberOfSiblings) {
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
	
	/*
	public String testprint(int level) {
		String indentLevelString = "";
		for ( int i = 0; i < level; i++ ) {
			indentLevelString += levelIndentBase;
		}
		return indentLevelString;
	}
	*/
	
	public void planWalk( final LogicalPlan plan, int planNumber, int planLevel, int numberOfSiblings, IndentingPrintStream out) {
		//System.out.println(getIndentLevelString2(planLevel, planNumber, numberOfSiblings) + plan.getRootOperator().toString() +" "+planLevel + " " + planNumber + " "+ numberOfSiblings);
		//System.out.println(getIndentLevelString2(planLevel, planNumber, numberOfSiblings) + plan.getRootOperator().toString() );
		//System.out.println(plan.getRootOperator().printString(getIndentLevelString2(planLevel, planNumber, numberOfSiblings)));
		String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings);
		if (planNumber < numberOfSiblings-1) {
			//out.append( plan.getRootOperator().printString(indentLevelString,   levelIndentBase) );
			out.append( plan.getRootOperator().toPrintString(indentLevelString) );
		}
		else {
			//out.append( plan.getRootOperator().printString(indentLevelString,   levelIndentBase) );
			out.append( plan.getRootOperator().toPrintString(indentLevelString) );
		}
		out.append(System.lineSeparator());
		planLevel += 1;
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel, plan.numberOfSubPlans(), out );
		}
		
	}

}
