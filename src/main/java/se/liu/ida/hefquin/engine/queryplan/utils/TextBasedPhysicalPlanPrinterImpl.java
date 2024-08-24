package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalOperatorForLogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class TextBasedPhysicalPlanPrinterImpl extends BaseForTextBasedPlanPrinters  implements PhysicalPlanPrinter
{	
	@Override
	public void print( final PhysicalPlan plan, final PrintStream out ) {
		planWalk(plan, 0, 0, 1, out, "");
		out.flush();
	}

	/**
	 * This method recursively goes through a plan, and appends specific strings to a print stream.
	 * @param plan The current plan (root operator) that will be formatted.
	 * @param planNumber The number of a plan in terms of its super plan.
	 * @param planLevel The depth of the root operator in a plan.
	 * @param numberOfSiblings The number of sibling plans of a plan.
	 * @param out The print stream that will print a plan.
	 */
	public void planWalk( final PhysicalPlan plan,
	                      final int planNumber,
	                      final int planLevel,
	                      final int numberOfSiblings,
	                      final PrintStream out,
	                      final String rootOpIndentString ) {
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, plan.numberOfSubPlans(), indentLevelString);

		final PhysicalOperator rootOp = plan.getRootOperator();
		if ( rootOp instanceof PhysicalOpBinaryUnion binaryUnionOp ) {
			printOp( binaryUnionOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithFILTER physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithUNION physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithVALUES physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpFilter physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpGlobalToLocal physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpHashJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpHashRJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpIndexNestedLoopsJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpLocalToGlobal physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpMultiwayUnion physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpNaiveNestedLoopsJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpParallelMultiLeftJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpRequest physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpSymmetricHashJoin physicalOp ) {
			printOp( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}

		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out, indentLevelString );
		}
	}

	protected void printOp( final PhysicalOpBinaryUnion op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "binaryUnion (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
	}

	protected void printOp( final PhysicalOpBindJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "bindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		printOperatorInfoFmAndPattern( op, out, indentLevelStringForOpDetail );
	}

	protected void printOp( final PhysicalOpBindJoinWithFILTER op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "FILTERBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		printOperatorInfoFmAndPattern( op, out, indentLevelStringForOpDetail );
	}

	protected void printOp( final PhysicalOpBindJoinWithUNION op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "UNIONBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		printOperatorInfoFmAndPattern( op, out, indentLevelStringForOpDetail );
	}

	protected void printOp( final PhysicalOpBindJoinWithVALUES op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "VALUESBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		printOperatorInfoFmAndPattern( op, out, indentLevelStringForOpDetail );
	}

	protected void printOp( final PhysicalOpFilter op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final LogicalOpFilter lop = (LogicalOpFilter) op.getLogicalOperator();

		out.append( indentLevelString + "filter (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		out.append( " filterExpressions (" + lop.getFilterExpressions().toString() + ")" );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final PhysicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final LogicalOpGlobalToLocal lop = (LogicalOpGlobalToLocal)  op.getLogicalOperator();

		out.append( indentLevelString + "g2l (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		out.append( "  vocab.mapping (" + lop.getVocabularyMapping().hashCode() + ")" );
		out.append( System.lineSeparator() );	
	}

	protected void printOp( final PhysicalOpHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
	}

	protected void printOp( final PhysicalOpHashRJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashRJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
	}

	protected void printOp( final PhysicalOpIndexNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "indexNestedLoop (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		printOperatorInfoFmAndPattern( op, out, indentLevelStringForOpDetail );
	}

	protected void printOp( final PhysicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final LogicalOpGlobalToLocal lop = (LogicalOpGlobalToLocal) op.getLogicalOperator();

		out.append( indentLevelString + "l2g (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
		out.append( "  vocab.mapping (" + lop.getVocabularyMapping().hashCode() + ")" );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final PhysicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "multiwayUnion (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
	}

	protected void printOp( final PhysicalOpNaiveNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "naiveNestedLoop (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
	}

	protected void printOp( final PhysicalOpParallelMultiLeftJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "parallelMultiLeftJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final PhysicalOpRequest<?,?> op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final LogicalOpRequest<?,?> lop = op.getLogicalOperator();
		final DataRetrievalRequest req = lop.getRequest();
		out.append( indentLevelString + "req (" + op.getID() + ")" );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") (" + req.toString() + ")" );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final PhysicalOpSymmetricHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "symmetricHashJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperator( op, out, indentLevelStringForOpDetail + singleBase );
	}

	protected void printLogicalOperator( final PhysicalOperatorForLogicalOperator pop,
	                                     final PrintStream out,
	                                     final String indentLevelString ) {
		printLogicalOperatorBase( "  - lop ", pop.getLogicalOperator(), out, indentLevelString );
		out.append( System.lineSeparator() );
	}

	protected void printOperatorInfoFmAndPattern( final PhysicalOperatorForLogicalOperator pop,
	                                              final PrintStream out,
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
		printSPARQLGraphPattern( gp, indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

}
