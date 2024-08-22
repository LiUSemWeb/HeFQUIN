package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;

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
				/*
				for ( int i = 1; i < planLevel; i++ ) {
					indentLevelString += spaceBase;
				}*/
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
}
