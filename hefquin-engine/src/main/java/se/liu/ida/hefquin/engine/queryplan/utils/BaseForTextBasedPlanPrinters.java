package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

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
			String indentLevelString = "";
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
		else if ( indentLevelString.endsWith(lastChildIndentBase) && indentLevelString.startsWith(" ") ) {
			return indentLevelString.replaceAll( ".", " " );
		}
		else if ( indentLevelString.endsWith(lastChildIndentBase) && indentLevelString.startsWith(levelIndentBase) ) {
			return indentLevelString.substring( 0, indentLevelString.length() - lastChildIndentBase.length() ) + spaceBase;
		}
		else if ( indentLevelString.equals(lastChildIndentBase) ) {
			return indentLevelString.replaceAll( ".", " " );
		}

		return "";
	}

	protected static void printFederationMember( final FederationMember fm,
	                                             final String indentString,
	                                             final PrintStream out ) {
		out.append( indentString );
		out.append( "  - fm (" + fm.getInterface().getID() + ") " + fm.getInterface().toString() );
		out.append( System.lineSeparator() );
	}

	protected static void printSPARQLGraphPattern( final SPARQLGraphPattern gp,
	                                               final String indentString,
	                                               final PrintStream out ) {
		out.append( indentString );
		out.append( "  - pattern (" + gp.hashCode() +  ") (" + gp.toString() + ")" );
		out.append( System.lineSeparator() );
	}


	protected static void printLogicalOperatorBase( final LogicalOperator lop,
	                                                final String indentString,
	                                                final PrintStream out,
	                                                final OpNamePrinter np ) {
		out.append( indentString );
		lop.visit(np);
		out.append( " (" + lop.getID()  + ")" );
	}

	protected static class OpNamePrinter implements LogicalPlanVisitor {
		protected final PrintStream out;
		public OpNamePrinter( final PrintStream out ) { this.out = out; }

		@Override
		public void visit( final LogicalOpRequest<?, ?> op )    { out.append("req"); }

		@Override
		public void visit( final LogicalOpTPAdd op )            { out.append("tpAdd"); }

		@Override
		public void visit( final LogicalOpBGPAdd op )           { out.append("bgpAdd"); }

		@Override
		public void visit( final LogicalOpGPAdd op )            { out.append("gpAdd"); }

		@Override
		public void visit( final LogicalOpTPOptAdd op )         { out.append("tpOptAdd"); }

		@Override
		public void visit( final LogicalOpBGPOptAdd op )        { out.append("bgpOptAdd"); }

		@Override
		public void visit( final LogicalOpGPOptAdd op )         { out.append("gpOptAdd"); }

		@Override
		public void visit( final LogicalOpJoin op )             { out.append("join"); }

		@Override
		public void visit( final LogicalOpRightJoin op )        { out.append("rightJoin"); }

		@Override
		public void visit( final LogicalOpUnion op )            { out.append("union"); }

		@Override
		public void visit( final LogicalOpMultiwayJoin op )     { out.append("mj"); }

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) { out.append("mlj"); }

		@Override
		public void visit( final LogicalOpMultiwayUnion op )    { out.append("mu"); }

		@Override
		public void visit( final LogicalOpFilter op )           { out.append("filter"); }

		@Override
		public void visit( final LogicalOpBind op )             { out.append("bind"); }

		@Override
		public void visit( final LogicalOpLocalToGlobal op )    { out.append("l2g"); }

		@Override
		public void visit( final LogicalOpGlobalToLocal op )    { out.append("g2l"); }
	}

}
