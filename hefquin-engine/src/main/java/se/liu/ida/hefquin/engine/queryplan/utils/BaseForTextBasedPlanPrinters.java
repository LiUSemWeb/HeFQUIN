package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.utils.PlanPrinter.PrintablePlan;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanProperty;
import se.liu.ida.hefquin.engine.queryplan.info.QueryPlanningInfo;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class BaseForTextBasedPlanPrinters
{
	public static final ShortNameCreator snc = new ShortNameCreator();

	/**
	 * An extension of {@link PrintablePlan} objects that makes it possible
	 * to record the graph pattern of the root operator of the plan (if any)
	 * as well as a full-string representation of that pattern. We use the
	 * latter only for cases in which the full-string representation of the
	 * pattern was too long to be put as a root-operator property (in which
	 * case the property that we put is only a shortened version of the full
	 * string).
	 */
	public static class ExtPrintablePlan extends PrintablePlan {
		public final SPARQLGraphPattern graphPattern;
		public final String fullStringForGraphPattern;

		public ExtPrintablePlan( final String rootOpAsString,
		                         final List<String> rootOpProperties,
		                         final List<PrintablePlan> subPlans,
		                         final SPARQLGraphPattern graphPattern,
		                         final String fullStringForGraphPattern ) {
			super(rootOpAsString, rootOpProperties, subPlans);
			this.graphPattern = graphPattern;
			this.fullStringForGraphPattern = fullStringForGraphPattern;
		}
	}

	public void printFullStringsForGraphPatterns( final ExtPrintablePlan pp,
	                                              final PrintStream out ) {
		printFullStringsForGraphPatterns( pp, new HashSet<>(), out );
	}

	public void printFullStringsForGraphPatterns( final ExtPrintablePlan pp,
	                                              final Set<SPARQLGraphPattern> alreadyPrinted,
	                                              final PrintStream out ) {
		if (    pp.fullStringForGraphPattern != null
		     && ! alreadyPrinted.contains(pp.graphPattern) ) {
			alreadyPrinted.add(pp.graphPattern);

			out.println();
			out.print( "--- pattern (" + pp.graphPattern.hashCode() + ") " );
			out.print( pp.graphPattern.getClass().getName() );
			out.println();
			out.println( pp.fullStringForGraphPattern );
		}

		if ( pp.subPlans != null ) {
			for ( final PrintablePlan sub : pp.subPlans ) {
				printFullStringsForGraphPatterns( (ExtPrintablePlan) sub,
				                                  alreadyPrinted,
				                                  out );
			}
		}
	}

	protected void addPropStrings( final ExpectedVariables expVars,
	                               final List<String> propStrings ) {
		if ( expVars == null ) {
			propStrings.add( "expected variables: ?" );
			return;
		}

		String certainVarsStr = "certain variables: ";
		final Iterator<Var> itC = expVars.getCertainVariables().iterator();
		certainVarsStr += itC.hasNext() ? itC.next().toString() : "none";
		while ( itC.hasNext() ) {
			certainVarsStr += ", " + itC.next().toString();
		}

		String possibleVarsStr = "possible variables: ";
		final Iterator<Var> itP = expVars.getPossibleVariables().iterator();
		possibleVarsStr += itP.hasNext() ? itP.next().toString() : "none";
		while ( itP.hasNext() ) {
			possibleVarsStr += ", " + itP.next().toString();
		}

		propStrings.add(certainVarsStr);
		propStrings.add(possibleVarsStr);
	}

	protected void addPropStrings( final QueryPlanningInfo qpInfo,
	                               final List<String> propStrings ) {
		if ( qpInfo == null || qpInfo.isEmpty() ) {
			propStrings.add("query planning info: none" );
			return;
		}

		final Iterator<QueryPlanProperty> it = qpInfo.getProperties().iterator();
		propStrings.add( "query planning info:  " + it.next().toString() );

		while ( it.hasNext() ) {
			propStrings.add( "                      " + it.next().toString() );
		}
	}


	public static class ShortNameCreator implements LogicalPlanVisitor {
		/**
		 * The short name of the most recently visited operator.
		 */
		public String name = null;

		@Override
		public void visit( final LogicalOpRequest<?, ?> op )    { name = "req"; }

		@Override
		public void visit( final LogicalOpFixedSolMap op )      { name = "sm"; }

		@Override
		public void visit( final LogicalOpGPAdd op )            { name = "gpAdd"; }

		@Override
		public void visit( final LogicalOpGPOptAdd op )         { name = "gpOptAdd"; }

		@Override
		public void visit( final LogicalOpJoin op )             { name = "join"; }

		@Override
		public void visit( final LogicalOpRightJoin op )        { name = "rightJoin"; }

		@Override
		public void visit( final LogicalOpUnion op )            { name = "union"; }

		@Override
		public void visit( final LogicalOpMultiwayJoin op )     { name = "mj"; }

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) { name = "mlj"; }

		@Override
		public void visit( final LogicalOpMultiwayUnion op )    { name = "mu"; }

		@Override
		public void visit( final LogicalOpFilter op )           { name = "filter"; }

		@Override
		public void visit( final LogicalOpBind op )             { name = "bind"; }

		@Override
		public void visit( final LogicalOpLocalToGlobal op )    { name = "l2g"; }

		@Override
		public void visit( final LogicalOpGlobalToLocal op )    { name = "g2l"; }
	}

}
