package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.utils.IndentingPrintStream;

public class TextBasedPhysicalPlanPrinterImpl2 implements PhysicalPlanPrinter
{
	// The string represents '|   '.
	private static String levelIndentBase = "\u2502   ";
	// The string represents '├── '.
	private static String nonLastChildIndentBase = "\u251C\u2500\u2500 ";
	// The string represents '└── '.
	private static String lastChildIndentBase = "\u2514\u2500\u2500 ";
	private static String spaceBase = "    ";
		
	@Override
	public void print( final PhysicalPlan plan, final PrintStream out ) {
		planWalk(plan, 0, 0, 1, out, "");
		out.flush();
	}

	public String getIndentLevelString(final int planNumber, final int planLevel, final int numberOfSiblings, final String upperRootOpIndentString) {
		String indentLevelString = "";
		if ( planLevel == 0 ) {
			// This is only for the root operator of the overall plan to be printed.
			return "";
		}
		else {
			if ( upperRootOpIndentString == "" ) {
				if ( planNumber < numberOfSiblings-1 ) {
					return indentLevelString + nonLastChildIndentBase;
				}
				else {
					return indentLevelString + lastChildIndentBase;
				}
			}
			else if ( upperRootOpIndentString.endsWith(nonLastChildIndentBase) ) {
				for ( int i = 1; i < planLevel; i++ ) {
					indentLevelString += levelIndentBase;
				}
				if ( planNumber < numberOfSiblings-1 ) {
					return indentLevelString + nonLastChildIndentBase;
				}
				else {
					return indentLevelString + lastChildIndentBase;
				}
			}
			else if ( upperRootOpIndentString.endsWith(lastChildIndentBase) ) {
				for ( int i = 1; i < planLevel; i++ ) {
					indentLevelString += spaceBase;
				}
				if ( planNumber < numberOfSiblings-1 ) {
					return indentLevelString + nonLastChildIndentBase;
				}
				else {
					return indentLevelString + lastChildIndentBase;
				}
			}
			else {
				return indentLevelString;
			}
		}
	}
	
	public String getIndentLevelStringForDetail(final int planNumber, final int planLevel, final int numberOfSiblings, final String indentLevelString) {
		String indentLevelStringForDetail = "";
		if ( planLevel == 0 ) {
			return spaceBase;
		}
		if ( indentLevelString == "") {
			indentLevelStringForDetail += spaceBase;
		}
		else if ( indentLevelString.endsWith(nonLastChildIndentBase) ) {
			indentLevelStringForDetail = indentLevelString.substring( 0, indentLevelString.length() - nonLastChildIndentBase.length() ) + levelIndentBase;
		}
		else if ( indentLevelString.endsWith(lastChildIndentBase) && indentLevelString.startsWith(" ") ) {
			indentLevelStringForDetail = indentLevelString.replaceAll( ".", " " );
		}
		else if ( indentLevelString.endsWith(lastChildIndentBase) && indentLevelString.startsWith(levelIndentBase) ) {
			indentLevelStringForDetail = indentLevelString.substring( 0, indentLevelString.length() - lastChildIndentBase.length() ) + spaceBase;
		}
		else if ( indentLevelString.equals(lastChildIndentBase) ) {
			indentLevelStringForDetail = indentLevelString.replaceAll( ".", " " );
		}
		return indentLevelStringForDetail;
	}
	
	/**
	 * This method recursively goes through a plan, and appends specific strings to a print stream.
	 * @param plan The current plan (root operator) that will be formatted.
	 * @param planNumber The number of a plan in terms of its super plan.
	 * @param planLevel The depth of the root operator in a plan.
	 * @param numberOfSiblings The number of sibling plans of a plan.
	 * @param out The print stream that will print a plan.
	 */
	public void planWalk( final PhysicalPlan plan, final int planNumber, final int planLevel, final int numberOfSiblings, final PrintStream out, final String rootOpIndentString ) {
		final PhysicalOperator rootOp = plan.getRootOperator();
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, indentLevelString);
		out.append( indentLevelString + rootOp.toString() );
		out.append( System.lineSeparator() );
		if ( rootOp instanceof PhysicalOpBinaryUnion ) {
			
		}
		else if ( rootOp instanceof PhysicalOpBindJoin ) {}
		else if ( rootOp instanceof PhysicalOpBindJoinWithFILTER ) {}
		else if ( rootOp instanceof PhysicalOpBindJoinWithUNION ) {}
		else if ( rootOp instanceof PhysicalOpBindJoinWithVALUES ) {}
		else if ( rootOp instanceof PhysicalOpFilter ) {}
		else if ( rootOp instanceof PhysicalOpGlobalToLocal ) {}
		else if ( rootOp instanceof PhysicalOpHashJoin ) {}
		else if ( rootOp instanceof PhysicalOpHashRJoin ) {}
		else if ( rootOp instanceof PhysicalOpIndexNestedLoopsJoin ) {}
		else if ( rootOp instanceof PhysicalOpLocalToGlobal ) {}
		else if ( rootOp instanceof PhysicalOpMultiwayUnion ) {}
		else if ( rootOp instanceof PhysicalOpNaiveNestedLoopsJoin ) {}
		else if ( rootOp instanceof PhysicalOpParallelMultiLeftJoin ) {}
		else if ( rootOp instanceof PhysicalOpRequest ) {}
		else if ( rootOp instanceof PhysicalOpSymmetricHashJoin ) {}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out, indentLevelString );
		}
	}
	
	protected void printOperatorInfoForBinaryUnion ( final PhysicalOpBinaryUnion op, final PrintStream out, final String indentLevelString ) {
		//out.append( indentLevelString + "binaryUnion (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBindJoin ( final PhysicalOpBindJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		//out.append( indentLevelString + "bindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBindJoinWithFILTER ( final PhysicalOpBindJoinWithFILTER op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		//out.append( indentLevelString + "FILTERBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
}
