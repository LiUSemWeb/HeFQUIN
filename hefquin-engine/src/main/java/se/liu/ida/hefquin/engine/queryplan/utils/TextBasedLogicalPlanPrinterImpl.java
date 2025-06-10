package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

/**
 * Internally, the functionality of this class is implemented based on
 * a {@link LogicalPlanVisitor}, which makes sure that we get a compiler
 * error whenever we add a new type of logical operator but forget to
 * extend this class here to cover that new operator.
 */
public class TextBasedLogicalPlanPrinterImpl extends BaseForTextBasedPlanPrinters implements LogicalPlanPrinter
{
	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
		final OpPrinter opPrinter = new OpPrinter(out);
		planWalk(plan, 0, 0, 1, opPrinter, "");
		out.flush();
	}

	/**
	 * This method recursively goes through a plan, and appends specific strings to a print stream.
	 * @param plan The current plan (root operator) that will be formatted.
	 * @param planNumber The number of a plan in terms of its super plan.
	 * @param planLevel The depth of the root operator in a plan.
	 * @param numberOfSiblings The number of sibling plans of a plan.
	 * @param opPrinter The helper object for the printing.
	 */
	public void planWalk( final LogicalPlan plan,
	                      final int planNumber,
	                      final int planLevel,
	                      final int numberOfSiblings,
	                      final OpPrinter opPrinter,
	                      final String rootOpIndentString ) {
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		opPrinter.setIndentLevelString(indentLevelString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, plan.numberOfSubPlans(), indentLevelString);
		opPrinter.setIndentLevelStringForOpDetail(indentLevelStringForOpDetail);

		plan.getRootOperator().visit(opPrinter);

		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), opPrinter, indentLevelString );
		}
	}

	protected static class OpPrinter implements LogicalPlanVisitor {
		protected final PrintStream out;
		protected final OpNamePrinter np;
		protected String indentLevelString = null;
		protected String indentLevelStringForOpDetail = null;

		public OpPrinter( final PrintStream out ) {
			this.out = out;
			this.np = new OpNamePrinter(out);
		}

		public void setIndentLevelString( final String s ) { indentLevelString = s; }
		public void setIndentLevelStringForOpDetail( final String s ) { indentLevelStringForOpDetail = s; }

		@Override
		public void visit( final LogicalOpBGPAdd op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpBGPOptAdd op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpBind op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );

			final VarExprList bindExpressions = op.getBindExpressions();
			for ( Map.Entry<Var, Expr> e : bindExpressions.getExprs().entrySet() ) {
				final Var var = e.getKey();
				final Expr expr = e.getValue();
				out.append( indentLevelStringForOpDetail + singleBase );
				out.append( "  - " + var.toString() + " <-- " + ExprUtils.fmtSPARQL(expr) );
				out.append( System.lineSeparator() );
			}

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpFilter op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );

			printExpressions( op.getFilterExpressions(),
			                  indentLevelStringForOpDetail + singleBase,
			                  out );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpGlobalToLocal op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpGPAdd op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpGPOptAdd op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpJoin op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpLocalToGlobal op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpMultiwayLeftJoin op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpRequest<?,?> op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail, out );
			final DataRetrievalRequest req = op.getRequest();
			out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") " + req.toString() );
			out.append( System.lineSeparator() );
			out.append( indentLevelStringForOpDetail );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpRightJoin op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpTPAdd op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpTPOptAdd op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
			out.append( System.lineSeparator() );
			printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final LogicalOpUnion op ) {
			printLogicalOperatorBase( op, indentLevelString, out, np );
		}
	}

}
