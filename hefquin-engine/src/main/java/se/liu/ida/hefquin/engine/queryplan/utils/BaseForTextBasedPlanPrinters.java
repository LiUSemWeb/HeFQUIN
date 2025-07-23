package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
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
		protected ExpectedVariables expVars = null;
		protected QueryPlanningInfo qpInfo = null;

		protected Set<SPARQLGraphPattern> graphPatterns = new HashSet<>();
		protected List<String> fullStringsForGraphPatterns = new ArrayList<>();

		public OpPrinterBase( final PrintStream out ) {
			this.out = out;
			this.np = new OpNamePrinter(out);
		}

		public void setIndentLevelString( final String s ) { indentLevelString = s; }
		public void setIndentLevelStringForOpDetail( final String s ) { indentLevelStringForOpDetail = s; }
		public void setExpectedVariables( final ExpectedVariables expVars ) { this.expVars = expVars; }
		public void setQueryPlanningInfo( final QueryPlanningInfo qpInfo ) { this.qpInfo = qpInfo; }

		public void printExpectedVariables( final String indentString ) {
			if ( expVars == null ) {
				out.append( indentString );
				out.append( "  - expected variables: ?" );
				out.append( System.lineSeparator() );
				return;
			}

			String certainVarsStr = "  - certain variables: ";
			final Iterator<Var> itC = expVars.getCertainVariables().iterator();
			certainVarsStr += itC.hasNext() ? itC.next().toString() : "none";
			while ( itC.hasNext() ) {
				certainVarsStr += ", " + itC.next().toString();
			}

			out.append( indentString + certainVarsStr + System.lineSeparator() );

			String possibleVarsStr = "  - possible variables: ";
			final Iterator<Var> itP = expVars.getPossibleVariables().iterator();
			possibleVarsStr += itP.hasNext() ? itP.next().toString() : "none";
			while ( itP.hasNext() ) {
				possibleVarsStr += ", " + itP.next().toString();
			}

			out.append( indentString + possibleVarsStr + System.lineSeparator() );
		}

		public void printQueryPlanningInfo( final String indentString ) {
			out.append( indentString );
			out.append( "  - query planning info: " );

			if ( qpInfo == null || qpInfo.isEmpty() ) {
				out.append( "none" + System.lineSeparator() );
			}
			else {
				final Iterator<QueryPlanProperty> it = qpInfo.getProperties().iterator();
				out.append( " " );
				print( it.next() );
				out.append( System.lineSeparator() );

				while ( it.hasNext() ) {
					out.append( indentString );
					out.append( "                          " );
					print( it.next() );
					out.append( System.lineSeparator() );
				}
			}
		}

		protected void print( final QueryPlanProperty prop ) {
			out.append( prop.getType().name + " = " + prop.getValue() );
			switch ( prop.getQuality() ) {
			case PURE_GUESS:                   out.append(" (pure guess)"); break;
			case MIN_OR_MAX_POSSIBLE:          out.append(" (min or max possible)"); break;
			case ESTIMATE_BASED_ON_ESTIMATES:  out.append(" (estimate based on estimates)"); break;
			case ESTIMATE_BASED_ON_ACCURATES:  out.append(" (estimate based on accurates)"); break;
			case DIRECT_ESTIMATE:              out.append(" (direct estimate)"); break;
			case ACCURATE:                     out.append(" (accurate)"); break;
			}
		}

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
