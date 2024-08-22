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
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
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
		if ( rootOp instanceof PhysicalOpBinaryUnion ) {
			printOperatorInfoForBinaryUnion( (PhysicalOpBinaryUnion) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoin ) {
			printOperatorInfoForBindJoin( (PhysicalOpBindJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithFILTER ) {
			printOperatorInfoForBindJoinWithFILTER( (PhysicalOpBindJoinWithFILTER) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithUNION ) {
			printOperatorInfoForBindJoinWithUNION( (PhysicalOpBindJoinWithUNION) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpBindJoinWithVALUES ) {
			printOperatorInfoForBindJoinWithVALUES( (PhysicalOpBindJoinWithVALUES) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpFilter ) {
			printOperatorInfoForOpFILTER( (PhysicalOpFilter) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpGlobalToLocal ) {
			printOperatorInfoForOpGlobalToLocal( (PhysicalOpGlobalToLocal) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpHashJoin ) {
			printOperatorInfoForOpHashJoin( (PhysicalOpHashJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpHashRJoin ) {
			printOperatorInfoForOpHashRJoin( (PhysicalOpHashRJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpIndexNestedLoopsJoin ) {
			printOperatorInfoForOpIndexNestedLoopsJoin( (PhysicalOpIndexNestedLoopsJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpLocalToGlobal ) {
			printOperatorInfoForOpLocalToGlobal( (PhysicalOpLocalToGlobal) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpMultiwayUnion ) {
			printOperatorInfoForMultiwayUnion( (PhysicalOpMultiwayUnion) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpNaiveNestedLoopsJoin ) {
			printOperatorInfoForOpNaiveNestedLoopsJoin( (PhysicalOpNaiveNestedLoopsJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpParallelMultiLeftJoin ) {
			printOperatorInfoForOpParallelMultiLeftJoin( (PhysicalOpParallelMultiLeftJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpRequest ) {
			printOperatorInfoForRequest( (PhysicalOpRequest) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof PhysicalOpSymmetricHashJoin ) {
			printOperatorInfoForOpSymmetricHashJoin( (PhysicalOpSymmetricHashJoin) rootOp, out, indentLevelString, indentLevelStringForOpDetail );
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
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForBindJoin ( final PhysicalOpBindJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "bindJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForBindJoinWithFILTER ( final PhysicalOpBindJoinWithFILTER op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "FILTERBindJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForBindJoinWithUNION ( final PhysicalOpBindJoinWithUNION op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "UNIONBindJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForBindJoinWithVALUES ( final PhysicalOpBindJoinWithVALUES op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "VALUESBindJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpFILTER ( final PhysicalOpFilter op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "filter (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpGlobalToLocal ( final PhysicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "g2l (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}

	protected void printOperatorInfoForOpHashJoin ( final PhysicalOpHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpHashRJoin ( final PhysicalOpHashRJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashRJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpIndexNestedLoopsJoin ( final PhysicalOpIndexNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashRJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}

	protected void printOperatorInfoForOpLocalToGlobal ( final PhysicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "l2g (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForMultiwayUnion ( final PhysicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "multiwayUnion (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}

	protected void printOperatorInfoForOpNaiveNestedLoopsJoin ( final PhysicalOpNaiveNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "naiveNestedLoop (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}

	protected void printOperatorInfoForOpParallelMultiLeftJoin ( final PhysicalOpParallelMultiLeftJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "parallelMultiLeftJoin (" + op.getID() + ") " );
		out.append( System.lineSeparator() );
		//printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForRequest( final PhysicalOpRequest op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final LogicalOpRequest lop = op.getLogicalOperator();
		final DataRetrievalRequest req = lop.getRequest();
		out.append( indentLevelString + "req (" + lop.getID() + ")" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") (" + req.toString() + ")" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForOpSymmetricHashJoin ( final PhysicalOpSymmetricHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "symmetricHashJoin (" + op.getID() + ") " );
		printLogicalOpForPhysicalOp( op.getLogicalOperator(), out, indentLevelString, indentLevelStringForOpDetail );
	}
	
	protected void printLogicalOpForPhysicalOp ( final LogicalOperator lop, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( System.lineSeparator() );
		if ( lop instanceof LogicalOpUnion ) {
			printLogicalOpUnionForPhysicalOp( (LogicalOpUnion) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpMultiwayUnion ) {
			printLogicalOpMultiwayUnionForPhysicalOp( (LogicalOpMultiwayUnion) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpTPAdd ) {
			printLogicalOpTpAddForPhysicalOp( (LogicalOpTPAdd) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			printLogicalOpTpOptAddForPhysicalOp( (LogicalOpTPOptAdd) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			printLogicalOpBGPAddForPhysicalOp( (LogicalOpBGPAdd) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpBGPOptAdd ) {
			printLogicalOpBGPOptAddForPhysicalOp( (LogicalOpBGPOptAdd) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpGPAdd ) {
			printLogicalOpGPAddForPhysicalOp( (LogicalOpGPAdd) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpGPOptAdd) {
			printLogicalOpGPOptAddForPhysicalOp( (LogicalOpGPOptAdd) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpFilter ) {
			printLogicalOpFilterForPhysicalOp( (LogicalOpFilter) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpGlobalToLocal ) {
			printLogicalOpGlobalToLocalForPhysicalOp( (LogicalOpGlobalToLocal) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpJoin ) {
			printLogicalOpJoinForPhysicalOp( (LogicalOpJoin) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpRightJoin ) {
			printLogicalOpRightJoinForPhysicalOp( (LogicalOpRightJoin) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpLocalToGlobal ) {
			printLogicalOpLocalToGlobalForPhysicalOp( (LogicalOpLocalToGlobal) lop, out, indentLevelStringForOpDetail );
		}
		else if ( lop instanceof LogicalOpRequest ) {
			printLogicalOpRequestForPhysicalOp( (LogicalOpRequest) lop, out, indentLevelStringForOpDetail );
		}
		else {
			throw new IllegalArgumentException( "Unexpected logical operator type: " + lop.getClass().getName() );
		}
	} 
	
	protected void printLogicalOpTpAddForPhysicalOp (final LogicalOpTPAdd lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") tpAdd" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( lop.getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printLogicalOpTpOptAddForPhysicalOp (final LogicalOpTPOptAdd lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") tpOptAdd" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( lop.getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printLogicalOpBGPAddForPhysicalOp (final LogicalOpBGPAdd lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") bgpAdd" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( lop.getBGP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printLogicalOpBGPOptAddForPhysicalOp (final LogicalOpBGPOptAdd lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") bgpOptAdd" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( lop.getBGP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printLogicalOpGPAddForPhysicalOp (final LogicalOpGPAdd lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") gpAdd" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( lop.getPattern(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printLogicalOpGPOptAddForPhysicalOp (final LogicalOpGPOptAdd lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") gpOptAdd" );
		out.append( System.lineSeparator() );
		printFederationMember( lop.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( lop.getPattern(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printLogicalOpUnionForPhysicalOp (final LogicalOpUnion lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") union" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printLogicalOpMultiwayUnionForPhysicalOp (final LogicalOpMultiwayUnion lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") mu" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printLogicalOpFilterForPhysicalOp (final LogicalOpFilter lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") filter" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - filterExpressions " + lop.getFilterExpressions().toString() );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	

	protected void printLogicalOpGlobalToLocalForPhysicalOp (final LogicalOpGlobalToLocal lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") g2l" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping " + lop.getVocabularyMapping().hashCode() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printLogicalOpJoinForPhysicalOp (final LogicalOpJoin lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") join" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printLogicalOpRightJoinForPhysicalOp (final LogicalOpRightJoin lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") rightjoin" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printLogicalOpLocalToGlobalForPhysicalOp (final LogicalOpLocalToGlobal lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") l2g" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping " + lop.getVocabularyMapping().hashCode() );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printLogicalOpRequestForPhysicalOp (final LogicalOpRequest lop, final PrintStream out, final String indentLevelStringForOpDetail) {
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + lop.getID() + ") req" );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
}
