package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
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

public class BaseForTextBasedPlanPrinters
{
	// The string represents '|'.
	protected static String singleBase = "\u2502";
	// The string represents '|   '.
	protected static String levelIndentBase = "\u2502   ";
	// The string represents '├── '.
	protected static String nonLastChildIndentBase = "\u251C\u2500\u2500 ";
	// The string represents '└── '.
	protected static String lastChildIndentBase = "\u2514\u2500\u2500 ";
	protected static String spaceBase = "    ";
	

	protected String getIndentLevelString(final int planNumber, final int planLevel, final int numberOfSiblings, final String upperRootOpIndentString) {
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
				indentLevelString = upperRootOpIndentString.substring( 0, upperRootOpIndentString.length() - lastChildIndentBase.length() ) + spaceBase;
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
	
	protected String getIndentLevelStringForDetail(final int planNumber, final int planLevel, final int numberOfSiblings, final int numberOfSubPlans, final String indentLevelString) {
		String indentLevelStringForDetail = "";
		if ( planLevel == 0 ) {
			if ( numberOfSubPlans > 0 ) {
				return "";
			}
			else {
				return spaceBase;
			}
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
	
	protected void printFederationMember( final FederationMember fm, final String indentLevelStringForOpDetail, final PrintStream out ) {
		out.append( indentLevelStringForOpDetail + "  - fm (" + fm.getInterface().getID() + ") " + fm.getInterface().toString() );
		out.append( System.lineSeparator() );
	}
	
	protected void printSPARQLGraphPattern (final SPARQLGraphPattern gp, final String indentLevelStringForOpDetail, final PrintStream out ) {
		out.append( indentLevelStringForOpDetail + "  - pattern (" + gp.hashCode() +  ") (" + gp.toString() + ")" );
		out.append( System.lineSeparator() );
	}
		
	protected void printLogicalOperatorBase(final String baseString, final LogicalOperator lop, final PrintStream out, String indentLevelString ) {
		out.append( indentLevelString + baseString + "" + nameOfLogicalOp(lop) + " (" + lop.getID()  + ")");
	}
	protected String nameOfLogicalOp ( final LogicalOperator lop) {
		if ( lop instanceof LogicalOpBGPAdd ) {
			return "bgpAdd";
		}
		else if ( lop instanceof LogicalOpBGPOptAdd ) {
			return "bgpOptAdd";
		}
		else if ( lop instanceof LogicalOpBind ) {
			return "bind";
		}
		else if ( lop instanceof LogicalOpFilter ) {
			return "filter";
		}
		else if ( lop instanceof LogicalOpGlobalToLocal ) {
			return "g2l";
		}
		else if ( lop instanceof LogicalOpGPAdd ) {
			return "gpAdd";
		}
		else if ( lop instanceof LogicalOpGPOptAdd ) {
			return "gpOptAdd";
		}
		else if ( lop instanceof LogicalOpJoin ) {
			return "join";
		}
		else if ( lop instanceof LogicalOpLocalToGlobal ) {
			return "l2g";
		}
		else if ( lop instanceof LogicalOpMultiwayJoin ) {
			return "mj";
		}
		else if ( lop instanceof LogicalOpMultiwayLeftJoin ) {
			return "mlj";
		}
		else if ( lop instanceof LogicalOpMultiwayUnion ) {
			return "mu";
		}
		else if ( lop instanceof LogicalOpUnion ) {
			return "union";
		}
		else if ( lop instanceof LogicalOpMultiwayUnion ) {
			return "mu";
		}
		else if ( lop instanceof LogicalOpRequest ) {
			return "req";
		}
		else if ( lop instanceof LogicalOpRightJoin ) {
			return "rightjoin";
		}
		else if ( lop instanceof LogicalOpTPAdd ) {
			return "tpAdd";
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			return "tpOptAdd";
		}
		else {
			throw new IllegalArgumentException( "Unexpected logical operator type: " + lop.getClass().getName() );
		}
	}

}
