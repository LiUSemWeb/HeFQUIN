package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

public class TextBasedLogicalPlanPrinterImpl2 implements LogicalPlanPrinter
{
	// The string represents '|   '.
	private static String levelIndentBase = "\u2502   ";
	// The string represents '├── '.
	private static String nonLastChildIndentBase = "\u251C\u2500\u2500 ";
	// The string represents '└── '.
	private static String lastChildIndentBase = "\u2514\u2500\u2500 ";
	private static String spaceBase = "    ";
		
	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
		planWalk(plan, 0, 0, 1, out, "");
		out.flush();	
	}
	
	public String getIndentLevelString(final int planNumber, final int planLevel, final int numberOfSiblings, final String upperRootOpIndentString) {
		String indentLevelString = "";
		if ( planLevel == 0 ) {
			// This is only for the root operator of the overall plan to be printed.
			//assert planLevel == 0;
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
	public void planWalk( final LogicalPlan plan, final int planNumber, final int planLevel, final int numberOfSiblings, final PrintStream out, final String rootOpIndentString ) {
		final LogicalOperator rootOp = plan.getRootOperator();
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, indentLevelString);
		if ( rootOp instanceof LogicalOpBGPAdd ) {
			printOperatorInfoForBGPAdd( (LogicalOpBGPAdd) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpBGPOptAdd ) {
			printOperatorInfoForBGPOptAdd( (LogicalOpBGPOptAdd) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpBind) {
			printOperatorInfoForBind( (LogicalOpBind) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpFilter ) {
			printOperatorInfoForFilter( (LogicalOpFilter) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpGlobalToLocal ) {
			printOperatorInfoForGlobalToLocal( (LogicalOpGlobalToLocal) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpGPAdd ) {
			printOperatorInfoForGPAdd( (LogicalOpGPAdd) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGPOptAdd ) {
			printOperatorInfoForGPOptAdd( (LogicalOpGPOptAdd) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpJoin ) {
			printOperatorInfoForJoin( (LogicalOpJoin) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpLocalToGlobal ) {
			printOperatorInfoForLocalToGlobal( (LogicalOpLocalToGlobal) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpMultiwayJoin ) {
			printOperatorInfoForMultiwayJoin( (LogicalOpMultiwayJoin) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpMultiwayLeftJoin ) {
			printOperatorInfoForMultiwayLeftJoin( (LogicalOpMultiwayLeftJoin) rootOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpMultiwayUnion) {
			printOperatorInfoMultiwayUnion( (LogicalOpMultiwayUnion) rootOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpRequest) {
			printOperatorInfoForRequest( (LogicalOpRequest) rootOp, out, indentLevelString, indentLevelStringForOpDetail );			
		}
		else if ( rootOp instanceof LogicalOpRightJoin ) {
			printOperatorInfoForRightJoin( (LogicalOpRightJoin) rootOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpTPAdd ) {
			printOperatorInfoForTPAdd( (LogicalOpTPAdd) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpTPOptAdd ) {
			printOperatorInfoForTPOptAdd( (LogicalOpTPOptAdd) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpUnion ) {
			printOperatorInfoForUnion( (LogicalOpUnion) rootOp, out, indentLevelString );
		}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out, indentLevelString );
		}
	}
	
	protected void printOperatorInfoForBGPAdd ( final LogicalOpBGPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "bgpAdd (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + levelIndentBase, out );
		printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + levelIndentBase, out );
		out.append( indentLevelStringForOpDetail + levelIndentBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBGPOptAdd ( final LogicalOpBGPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "bgpOptAdd (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + levelIndentBase, out );
		printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + levelIndentBase, out );
		out.append( indentLevelStringForOpDetail + levelIndentBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBind( final LogicalOpBind op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "bind (" + op.getID() + ") " + op.getBindExpressions().toString() );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForFilter( final LogicalOpFilter op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "filter (" + op.getID() + ") " + op.getFilterExpressions().toString() );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForGlobalToLocal( final LogicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "g2l (" + op.getID() + ") " + op.getVocabularyMapping().hashCode() );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForGPAdd ( final LogicalOpGPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "gpAdd (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + levelIndentBase, out );
		printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + levelIndentBase, out );
		out.append( indentLevelStringForOpDetail + levelIndentBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForGPOptAdd ( final LogicalOpGPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "gpOptAdd (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + levelIndentBase, out );
		printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + levelIndentBase, out );
		out.append( indentLevelStringForOpDetail + levelIndentBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForJoin( final LogicalOpJoin op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "join (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForLocalToGlobal( final LogicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "l2g (" + op.getID() + ") " + op.getVocabularyMapping().hashCode() );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForMultiwayJoin( final LogicalOpMultiwayJoin op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "mj (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForMultiwayLeftJoin( final LogicalOpMultiwayLeftJoin op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "mlj (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoMultiwayUnion( final LogicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "mu (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForRequest( final LogicalOpRequest op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final DataRetrievalRequest req = op.getRequest();
		out.append( indentLevelString + "req (" + op.getID() + ")" );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") " + req.toString() );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForRightJoin( final LogicalOpRightJoin op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "rightjoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForTPAdd ( final LogicalOpTPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "tpAdd (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + levelIndentBase, out );
		printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + levelIndentBase, out );
		out.append( indentLevelStringForOpDetail + levelIndentBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForTPOptAdd ( final LogicalOpTPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "tpOptAdd (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + levelIndentBase, out );
		printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + levelIndentBase, out );
		out.append( indentLevelStringForOpDetail + levelIndentBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForUnion( final LogicalOpUnion op, final PrintStream out, final String indentLevelString ) {
		out.append( indentLevelString + "union (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printFederationMember( final FederationMember fm, final String indentLevelStringForOpDetail, final PrintStream out ) {
		out.append( indentLevelStringForOpDetail + "  - fm (" + fm.getInterface().getID() + ") " + fm.getInterface().toString() );
		out.append( System.lineSeparator() );
	}
	
	protected void printSPARQLGraphPattern (final SPARQLGraphPattern gp, final String indentLevelStringForOpDetail, final PrintStream out ) {
		out.append( indentLevelStringForOpDetail + "  - pattern (" + gp.hashCode() +  ") " + gp.toString() );
		out.append( System.lineSeparator() );
	}
}
