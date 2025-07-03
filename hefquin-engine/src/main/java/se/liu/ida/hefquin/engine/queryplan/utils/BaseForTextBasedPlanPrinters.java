package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.federation.FederationMember;

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

	protected static void printFederationMember( final FederationMember fm,
	                                             final String indentString,
	                                             final PrintStream out ) {
		out.append( indentString );
		out.append( "  - fm (" + fm.getInterface().getID() + ") " + fm.getInterface().toString() );
		out.append( System.lineSeparator() );
	}

	protected static String printSPARQLGraphPattern( final SPARQLGraphPattern gp,
	                                                 final String indentString,
	                                                 final PrintStream out ) {
		final String gpAsString = gp.toStringForPlanPrinters();
		final String gpAsString2 = gpAsString.replaceAll( "\\s+", " ");
		final String gpAsShortString;
		if ( gpAsString2.length() > 88 )
			gpAsShortString = gpAsString2.substring(0, 40) + "[...]" + gpAsString2.substring( gpAsString2.length()-40 );
		else
			gpAsShortString = gpAsString2;

		out.append( indentString );
		out.append( "  - pattern (" + gp.hashCode() +  "): " + gpAsShortString );
		out.append( System.lineSeparator() );

		return gpAsString;
	}

	protected static void printExpressions( final ExprList exprs,
	                                        final String indentString,
	                                        final PrintStream out ) {
		final int numberOfExprs = exprs.size();
		if ( numberOfExprs == 1 ) {
			final Expr expr = exprs.get(0);
			out.append( indentString + "  - expression: " + ExprUtils.fmtSPARQL(expr) );
			out.append( System.lineSeparator() );
		}
		else {
			out.append( indentString + "  - number of expressions: " + numberOfExprs );
			out.append( System.lineSeparator() );
			for ( int i = 0; i < numberOfExprs; i++ ) {
				final Expr expr = exprs.get(i);
				out.append( indentString + "  - expression " + (i+1) + ": " + ExprUtils.fmtSPARQL(expr) );
				out.append( System.lineSeparator() );
			}
		}
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
		public void visit( final LogicalOpGPAdd op )            { out.append("gpAdd"); }

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

	protected static class OpPrinterBase {
		protected final PrintStream out;
		protected final OpNamePrinter np;

		protected String indentLevelString = null;
		protected String indentLevelStringForOpDetail = null;

		protected Set<SPARQLGraphPattern> graphPatterns = new HashSet<>();
		protected List<String> fullStringsForGraphPatterns = new ArrayList<>();

		public OpPrinterBase( final PrintStream out ) {
			this.out = out;
			this.np = new OpNamePrinter(out);
		}

		public void setIndentLevelString( final String s ) { indentLevelString = s; }
		public void setIndentLevelStringForOpDetail( final String s ) { indentLevelStringForOpDetail = s; }

		public void printFullStringsForGraphPatterns() {
			if ( fullStringsForGraphPatterns.isEmpty() )
				return;

			for ( final String s : fullStringsForGraphPatterns ) {
				out.append( s + System.lineSeparator() );
			}
		}

		protected void printSPARQLGraphPattern( final SPARQLGraphPattern gp,
		                                        final String indentString ) {
			final String full = BaseForTextBasedPlanPrinters.printSPARQLGraphPattern(gp, indentString, out);

			if ( ! graphPatterns.contains(gp) ) {
				final String full2 =
						"--- pattern (" + gp.hashCode() + ") "
						+ gp.getClass().getName()
						+ System.lineSeparator()
						+ full
						+ System.lineSeparator();
				fullStringsForGraphPatterns.add(full2);
			}
		}
	}

}
