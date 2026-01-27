package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.utils.PlanPrinter;
import se.liu.ida.hefquin.base.utils.PlanPrinter.PrintablePlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithBinaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithNullaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWithUnaryRoot;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.federation.access.SPARQLRequest;

/**
 * Internally, the functionality of this class is implemented based on
 * a {@link LogicalPlanVisitor}, which makes sure that we get a compiler
 * error whenever we add a new type of logical operator but forget to
 * extend this class here to cover that new operator.
 */
public class TextBasedLogicalPlanPrinterImpl extends BaseForTextBasedPlanPrinters
                                             implements LogicalPlanPrinter
{
	public static final MyPropertiesExtractor pe = new MyPropertiesExtractor();

	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
		final ExtPrintablePlan pp = createPrintablePlan(plan);
		PlanPrinter.print(pp, out);
		printFullStringsForGraphPatterns(pp, out);
		out.flush();
	}

	public ExtPrintablePlan createPrintablePlan( final LogicalPlan lp ) {
		final LogicalOperator rootOp = lp.getRootOperator();

		rootOp.visit(snc);
		final String rootOpString = snc.name + " (" + rootOp.getID()  + ")";

		pe.graphPattern = null;
		pe.fullStringForGraphPattern = null;
		pe.props = new ArrayList<>();

		rootOp.visit(pe);

		final SPARQLGraphPattern graphPattern  = pe.graphPattern;
		final String fullStringForGraphPattern = pe.fullStringForGraphPattern;
		final List<String> rootOpProps         = pe.props;

		addPropStrings( lp.getExpectedVariables(), rootOpProps );
		addPropStrings( lp.getQueryPlanningInfo(), rootOpProps );

		final List<PrintablePlan> subPlans = createPrintableSubPlans(lp);

		return new ExtPrintablePlan( rootOpString, rootOpProps, subPlans,
		                             graphPattern, fullStringForGraphPattern );
	}

	public List<PrintablePlan> createPrintableSubPlans( final LogicalPlan lp ) {
		if ( lp instanceof LogicalPlanWithNullaryRoot ) {
			return null;
		}
		else if ( lp instanceof LogicalPlanWithUnaryRoot u ) {
			final PrintablePlan pp = createPrintablePlan( u.getSubPlan() );
			return List.of(pp);
		}
		else if ( lp instanceof LogicalPlanWithBinaryRoot b ) {
			final PrintablePlan pp1 = createPrintablePlan( b.getSubPlan1() );
			final PrintablePlan pp2 = createPrintablePlan( b.getSubPlan2() );
			return List.of(pp1, pp2);
		}
		else if ( lp instanceof LogicalPlanWithNaryRoot n ) {
			final List<PrintablePlan> subPlans = new ArrayList<>( n.numberOfSubPlans() );
			final Iterator<LogicalPlan> it = n.getSubPlans();
			while ( it.hasNext() ) {
				final PrintablePlan pp = createPrintablePlan( it.next() );
				subPlans.add(pp);
			}

			return subPlans;
		}
		else {
			throw new IllegalArgumentException( lp.getClass().getName() );
		}
	}


	public static class MyPropertiesExtractor implements LogicalPlanVisitor {
		/**
		 * Strings representing the properties of the most recently visited
		 * operator.
		 */
		public List<String> props = null;

		/**
		 * The graph pattern of  the most recently visited operator (if any).
		 */
		public SPARQLGraphPattern graphPattern;

		/**
		 * A full-string representation of the graph pattern of the most
		 * recently visited operator (if any), but only if that string is
		 * too long to be put as a property into {@link #props} (in which
		 * case we put a shortened version of that string into {@link #props}).
		 */
		public String fullStringForGraphPattern;

		@Override
		public void visit( final LogicalOpRequest<?,?> op ) {
			record( op.getFederationMember() );
			record( op.getRequest() );
		}

		@Override
		public void visit( final LogicalOpFixedSolMap op ) {
			props.add( "solmap: " + op.getSolutionMapping().toString() );
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			record( op.getFederationMember() );

			final StringBuilder b = new StringBuilder("parameter variables:");
			if ( op.hasParameterVariables() ) {
				for ( final Var v : op.getParameterVariables().values() )
					b.append( " " + v.toString() );
			}
			else {
				b.append( " none" );
			}
			props.add( b.toString() );

			record( op.getPattern() );
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			record( op.getFederationMember() );
			record( op.getPattern() );
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			// nothing extra
		}

		@Override
		public void visit( final LogicalOpRightJoin op ) {
			// nothing extra
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			// nothing extra
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			// nothing extra
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			// nothing extra
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			// nothing extra
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			final int numberOfExprs = op.getFilterExpressions().size();
			if ( numberOfExprs == 1 ) {
				final Expr expr = op.getFilterExpressions().get(0);
				props.add( "expression: " + ExprUtils.fmtSPARQL(expr) );
			}
			else {
				props.add( "number of expressions: " + numberOfExprs );
				for ( int i = 0; i < numberOfExprs; i++ ) {
					final Expr expr = op.getFilterExpressions().get(i);
					props.add( "expression " + (i+1) + ": " + ExprUtils.fmtSPARQL(expr) );
				}
			}
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			props = new ArrayList<>();

			final VarExprList bindExpressions = op.getBindExpressions();
			for ( Map.Entry<Var, Expr> e : bindExpressions.getExprs().entrySet() ) {
				final Var var = e.getKey();
				final Expr expr = e.getValue();
				props.add( var.toString() + " <-- " + ExprUtils.fmtSPARQL(expr) );
			}
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			props.add( "vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			props.add( "vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
		}

		protected void record( final FederationMember fm ) {
			props.add( "fm (" + fm.getID() + ") " + fm.toString() );
		}

		protected void record( final DataRetrievalRequest req ) {
			if ( req instanceof SPARQLRequest sreq ) {
				record( sreq.getQueryPattern() );
			}
			else {
				props.add( "request (" + req.hashCode() +  "): " + req.toString() );
			}
		}

		protected void record( final SPARQLGraphPattern gp ) {
			final String gpAsString = gp.toStringForPlanPrinters();
			final String gpAsString2 = gpAsString.replaceAll( "\\s+", " ");

			if ( gpAsString2.length() > 88 ) {
				// shorten the string
				final String s = gpAsString2.substring(0, 40) + "[...]" + gpAsString2.substring( gpAsString2.length()-40 );
				props.add( "pattern (" + gp.hashCode() +  "): " + s );
				graphPattern = gp;
				fullStringForGraphPattern = gpAsString2;
			}
			else {
				// the string is short enough, no need to shorten it
				props.add( "pattern: " + gpAsString2 );
				graphPattern = gp;
				fullStringForGraphPattern = null;
			}
		}
	}

}
