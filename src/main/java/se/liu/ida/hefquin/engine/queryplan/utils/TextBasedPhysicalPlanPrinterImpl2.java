package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
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
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class TextBasedPhysicalPlanPrinterImpl2 extends BaseForTextBasedPlanPrinters  implements PhysicalPlanPrinter
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
	public void planWalk( final PhysicalPlan plan, final int planNumber, final int planLevel, final int numberOfSiblings, final PrintStream out, final String rootOpIndentString ) {
		final PhysicalOperator rootOp = plan.getRootOperator();
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, plan.numberOfSubPlans(), indentLevelString);
		if ( rootOp instanceof PhysicalOpBinaryUnion binaryUnionOp ) {
			printOperatorInfoForBinaryUnion( binaryUnionOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoin physicalOp ) {
			printOperatorInfoForBindJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithFILTER physicalOp ) {
			printOperatorInfoForBindJoinWithFILTER( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithUNION physicalOp ) {
			printOperatorInfoForBindJoinWithUNION( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithVALUES physicalOp ) {
			printOperatorInfoForBindJoinWithVALUES( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpFilter physicalOp ) {
			printOperatorInfoForOpFILTER( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpGlobalToLocal physicalOp ) {
			printOperatorInfoForOpGlobalToLocal( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpHashJoin physicalOp ) {
			printOperatorInfoForOpHashJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpHashRJoin physicalOp ) {
			printOperatorInfoForOpHashRJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpIndexNestedLoopsJoin physicalOp ) {
			printOperatorInfoForOpIndexNestedLoopsJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpLocalToGlobal physicalOp ) {
			printOperatorInfoForOpLocalToGlobal( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpMultiwayUnion physicalOp ) {
			printOperatorInfoForMultiwayUnion( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpNaiveNestedLoopsJoin physicalOp ) {
			printOperatorInfoForOpNaiveNestedLoopsJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpParallelMultiLeftJoin physicalOp ) {
			printOperatorInfoForOpParallelMultiLeftJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpRequest physicalOp ) {
			printOperatorInfoForRequest( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpSymmetricHashJoin physicalOp ) {
			printOperatorInfoForOpSymmetricHashJoin( physicalOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out, indentLevelString );
		}
	}
	
	protected void printOperatorInfoForBinaryUnion ( final PhysicalOpBinaryUnion op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "binaryUnion (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBindJoin ( final PhysicalOpBindJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "bindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
		if ( lop instanceof LogicalOpTPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if ( lop instanceof LogicalOpTPOptAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		}
		printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBindJoinWithFILTER ( final PhysicalOpBindJoinWithFILTER op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "FILTERBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBindJoinWithUNION ( final PhysicalOpBindJoinWithUNION op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "UNIONBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
		printOperatorInfoFmAndPatternWithoutOpt( lop, out, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForBindJoinWithVALUES ( final PhysicalOpBindJoinWithVALUES op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "VALUESBindJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
		printOperatorInfoFmAndPatternWithoutOpt( lop, out, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpFILTER ( final PhysicalOpFilter op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "filter (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail + singleBase );
		out.append( " filterExpressions (" + ((LogicalOpFilter) lop).getFilterExpressions().toString() + ")" );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForOpGlobalToLocal ( final PhysicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "g2l (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail + singleBase );
		out.append( "  vocab.mapping (" + ((LogicalOpGlobalToLocal) lop).getVocabularyMapping().hashCode() + ")" );
		out.append( System.lineSeparator() );	
	}

	protected void printOperatorInfoForOpHashJoin ( final PhysicalOpHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForOpHashRJoin ( final PhysicalOpHashRJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashRJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForOpIndexNestedLoopsJoin ( final PhysicalOpIndexNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "indexNestedLoop (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
		printOperatorInfoFmAndPatternWithOpt( lop, out, indentLevelStringForOpDetail );
	}

	protected void printOperatorInfoForOpLocalToGlobal ( final PhysicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "l2g (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail + singleBase );
		out.append( "  vocab.mapping (" + ((LogicalOpGlobalToLocal) lop).getVocabularyMapping().hashCode() + ")" );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForMultiwayUnion ( final PhysicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "multiwayUnion (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOperatorInfoForOpNaiveNestedLoopsJoin ( final PhysicalOpNaiveNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "naiveNestedLoop (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOperatorInfoForOpParallelMultiLeftJoin ( final PhysicalOpParallelMultiLeftJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "parallelMultiLeftJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForRequest( final PhysicalOpRequest op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final LogicalOpRequest lop = op.getLogicalOperator();
		final DataRetrievalRequest req = lop.getRequest();
		out.append( indentLevelString + "req (" + op.getID() + ")" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") (" + req.toString() + ")" );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", lop, out, indentLevelStringForOpDetail );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForOpSymmetricHashJoin ( final PhysicalOpSymmetricHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "symmetricHashJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		printLogicalOperatorBase( "  - lop ", op.getLogicalOperator(), out, indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoFmAndPatternWithOpt (final LogicalOperator lop, final PrintStream out, final String indentLevelStringForOpDetail ) {
		if ( lop instanceof LogicalOpTPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getTP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
		else if ( lop instanceof LogicalOpTPOptAdd addOp ) {
			printFederationMember( ((LogicalOpTPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getTP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
		else if (lop instanceof LogicalOpBGPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getBGP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
		else if (lop instanceof LogicalOpBGPOptAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getBGP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail );
			out.append( System.lineSeparator() );
		}
		else if (lop instanceof LogicalOpGPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getPattern(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
		else if (lop instanceof LogicalOpGPOptAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getPattern(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
	}
	
	protected void printOperatorInfoFmAndPatternWithoutOpt (final LogicalOperator lop, final PrintStream out, final String indentLevelStringForOpDetail ) {
		if ( lop instanceof LogicalOpTPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getTP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
		else if (lop instanceof LogicalOpBGPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getBGP(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
		else if (lop instanceof LogicalOpGPAdd addOp ) {
			printFederationMember( addOp.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( addOp.getPattern(), indentLevelStringForOpDetail + singleBase, out );
			out.append( indentLevelStringForOpDetail + singleBase );
			out.append( System.lineSeparator() );
		}
	}

}
