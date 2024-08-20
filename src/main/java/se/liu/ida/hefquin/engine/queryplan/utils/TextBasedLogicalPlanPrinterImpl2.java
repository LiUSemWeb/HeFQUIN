package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;

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
		if ( numberOfSiblings == 0 ) {
			// This is only for the root operator of the overall plan to be printed.
			assert planLevel == 0;
			return "";
		}
		
		String indentLevelString = "";
		for ( int i = 1; i < planLevel; i++ ) {
			indentLevelString += levelIndentBase;
		}
		if (planNumber < numberOfSiblings-1) {
			return indentLevelString + nonLastChildIndentBase;
		}
		else {
			return indentLevelString + lastChildIndentBase;
		}
	}
	
	public String getIndentLevelStringForDetail(final int planNumber, final int planLevel, final int numberOfSiblings) {
		String indentLevelString = "   ";
		for ( int i = planLevel-1; i > 0 ; i-- ) {
			if (planNumber < numberOfSiblings-1 ) {
				if ( i == planLevel-1 ) {
					// For op that has siblings but not the last in a plan sequence and the first part of string from the right side.
					indentLevelString = levelIndentBase;
				}
				else {
					indentLevelString = levelIndentBase + indentLevelString;
				}
			}
			indentLevelString = levelIndentBase + indentLevelString;
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
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings);
		final LogicalOperator rootOp = plan.getRootOperator();
		if ( rootOp instanceof LogicalOpMultiwayJoin ) {
			printOperatorInfoMultiwayJoin( (LogicalOpMultiwayJoin) rootOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpMultiwayUnion) {
			printOperatorInfoMultiwayUnion( (LogicalOpMultiwayUnion) rootOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpRequest) {
			printOperatorInfoForRequest( (LogicalOpRequest) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out );
		}
	}
	
	protected void printOperatorInfoForRequest( final LogicalOpRequest op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final DataRetrievalRequest req = op.getRequest();
		out.append( indentLevelString + "req (" + op.getID() + ")" );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.toString() + ")" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoMultiwayJoin( final LogicalOpMultiwayJoin op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "mj (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoMultiwayUnion( final LogicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "mu (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printFederationMember( final FederationMember fm, final String indentLevelStringForOpDetail, final PrintStream out ) {
		out.append( indentLevelStringForOpDetail + "  - fm (" + fm.getInterface().toString() + ")" );
		out.append( System.lineSeparator() );
	}
	
}
