package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;
import java.util.Map;

import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;

import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SPARQLRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

/**
 * Internally, the functionality of this class is implemented based on
 * a {@link PhysicalPlanVisitor}, which makes sure that we get a compiler
 * error whenever we add a new type of logical operator but forget to
 * extend this class here to cover that new operator.
 */
public class TextBasedPhysicalPlanPrinterImpl extends BaseForTextBasedPlanPrinters  implements PhysicalPlanPrinter
{	
	@Override
	public void print( final PhysicalPlan plan, final PrintStream out ) {
		final OpPrinter opPrinter = new OpPrinter(out);
		planWalk(plan, 0, 0, 1, opPrinter, "");
		opPrinter.printFullStringsForGraphPatterns();
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
	public void planWalk( final PhysicalPlan plan,
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

	protected static class OpPrinter extends OpPrinterBase implements PhysicalPlanVisitor {

		public OpPrinter( final PrintStream out ) {
			super(out);
		}

		@Override
		public void visit( final PhysicalOpBinaryUnion op ) {
			out.append( indentLevelString + "binary union (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
		}

		@Override
		public void visit( final PhysicalOpBindJoin op ) {
			out.append( indentLevelString + "brTPF bind join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
			printOperatorInfoFmAndPattern( op, indentLevelStringForOpDetail );
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithFILTER op ) {
			out.append( indentLevelString + "FILTER-based bind join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
			printOperatorInfoFmAndPattern( op, indentLevelStringForOpDetail );
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithUNION op ) {
			out.append( indentLevelString + "UNION-based bind join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
			printOperatorInfoFmAndPattern( op, indentLevelStringForOpDetail );
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithVALUES op ) {
			out.append( indentLevelString + "VALUES-based bind join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
			printOperatorInfoFmAndPattern( op, indentLevelStringForOpDetail );
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithVALUESorFILTER op ) {
			out.append( indentLevelString + "VALUES/FILTER-based bind join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
			printOperatorInfoFmAndPattern( op, indentLevelStringForOpDetail );
		}

		@Override
		public void visit( final PhysicalOpFilter op ) {
			final LogicalOpFilter lop = (LogicalOpFilter) op.getLogicalOperator();

			out.append( indentLevelString + "filter (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );

			printExpressions( lop.getFilterExpressions(),
			                  indentLevelStringForOpDetail + singleBase,
			                  out );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpBind op ) {
			final LogicalOpBind lop = (LogicalOpBind) op.getLogicalOperator();

			out.append( indentLevelString + "bind (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );

			final VarExprList bindExpressions = lop.getBindExpressions();
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
		public void visit( final PhysicalOpGlobalToLocal op ) {
			final LogicalOpGlobalToLocal lop = (LogicalOpGlobalToLocal)  op.getLogicalOperator();

			out.append( indentLevelString + "g2l (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );

			out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping (" + lop.getVocabularyMapping().hashCode() + ")" );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpHashJoin op ) {
			out.append( indentLevelString + "hash join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
		}

		@Override
		public void visit( final PhysicalOpHashRJoin op ) {
			out.append( indentLevelString + "right-outer hash join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
		}

		@Override
		public void visit( final PhysicalOpIndexNestedLoopsJoin op ) {
			out.append( indentLevelString + "indexNLJ (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
			printOperatorInfoFmAndPattern( op, indentLevelStringForOpDetail );
		}

		@Override
		public void visit( final PhysicalOpLocalToGlobal op ) {
			final LogicalOpGlobalToLocal lop = (LogicalOpGlobalToLocal) op.getLogicalOperator();

			out.append( indentLevelString + "l2g (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );

			out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping (" + lop.getVocabularyMapping().hashCode() + ")" );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpMultiwayUnion op ) {
			out.append( indentLevelString + "multiway union (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpNaiveNestedLoopsJoin op ) {
			out.append( indentLevelString + "naive NLJ (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );
		}

		@Override
		public void visit( final PhysicalOpParallelMultiLeftJoin op ) {
			out.append( indentLevelString + "parallel multiway left-outer join (" + op.getID() + ") " );
			out.append( System.lineSeparator() );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpRequest<?,?> op ) {
			out.append( indentLevelString + "req (" + op.getID() + ")" );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail, out, np );

			final LogicalOpRequest<?,?> lop = op.getLogicalOperator();
			printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail, out );

			final DataRetrievalRequest req = lop.getRequest();
			if ( req instanceof SPARQLRequest sreq ) {
				printSPARQLGraphPattern( sreq.getQueryPattern(), indentLevelStringForOpDetail );
			}
			else {
				out.append( indentLevelStringForOpDetail + "  - request (" + req.hashCode() +  "): " + req.toString() );
				out.append( System.lineSeparator() );
			}

			out.append( indentLevelStringForOpDetail );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpSymmetricHashJoin op ) {
			out.append( indentLevelString + "SHJ (" + op.getID() + ") " );
			out.append( System.lineSeparator() );
			printLogicalOperator( op, indentLevelStringForOpDetail + singleBase, out, np );

			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}

		protected void printOperatorInfoFmAndPattern( final PhysicalOperatorForLogicalOperator pop,
		                                              final String indentLevelStringForOpDetail ) {
			final LogicalOperator lop = pop.getLogicalOperator();
			final FederationMember fm;
			final SPARQLGraphPattern gp;
			if ( lop instanceof LogicalOpTPAdd addOp ) {
				fm = addOp.getFederationMember();
				gp = addOp.getTP();
			}
			else if ( lop instanceof LogicalOpTPOptAdd addOp ) {
				fm = addOp.getFederationMember();
				gp = addOp.getTP();
			}
			else if ( lop instanceof LogicalOpBGPAdd addOp ) {
				fm = addOp.getFederationMember();
				gp = addOp.getBGP();
			}
			else if ( lop instanceof LogicalOpBGPOptAdd addOp ) {
				fm = addOp.getFederationMember();
				gp = addOp.getBGP();
			}
			else if ( lop instanceof LogicalOpGPAdd addOp ) {
				fm = addOp.getFederationMember();
				gp = addOp.getPattern();
			}
			else if ( lop instanceof LogicalOpGPOptAdd addOp ) {
				fm = addOp.getFederationMember();
				gp = addOp.getPattern();
			}
			else {
				throw new IllegalArgumentException( "Unexpected logical operator: " + lop.getClass().getName() );
			}

			printFederationMember( fm, indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( gp, indentLevelStringForOpDetail + singleBase );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
	}


	protected static void printLogicalOperator( final PhysicalOperatorForLogicalOperator pop,
	                                            final String indentLevelString,
	                                            final PrintStream out,
	                                            final OpNamePrinter lopNP ) {
		printLogicalOperatorBase( pop.getLogicalOperator(),
		                          indentLevelString + "  - lop: ",
		                          out,
		                          lopNP );
		out.append( System.lineSeparator() );
	}

}
