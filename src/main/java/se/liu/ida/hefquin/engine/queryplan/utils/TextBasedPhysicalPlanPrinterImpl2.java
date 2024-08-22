package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import com.fasterxml.jackson.core.sym.Name;

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
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForBindJoin ( final PhysicalOpBindJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "bindJoin (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(lop), lop.getID(), out, indentLevelStringForOpDetail );
		if ( lop instanceof LogicalOpTPAdd ) {
			printFederationMember( ((LogicalOpTPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			printFederationMember( ((LogicalOpTPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );

		}
		printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
	}
	
	protected void printOperatorInfoForBindJoinWithFILTER ( final PhysicalOpBindJoinWithFILTER op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "FILTERBindJoin (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
		if ( lop instanceof LogicalOpTPAdd ) {
			printFederationMember( ((LogicalOpTPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			printFederationMember( ((LogicalOpTPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpBGPAdd ) {
			printFederationMember( ((LogicalOpBGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpBGPAdd) lop).getBGP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpBGPOptAdd ) {
			printFederationMember( ((LogicalOpBGPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpBGPOptAdd) lop).getBGP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpGPAdd ) {
			printFederationMember( ((LogicalOpGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpGPAdd) lop).getPattern(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpGPOptAdd ) {
			printFederationMember( ((LogicalOpGPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpGPOptAdd) lop).getPattern(), indentLevelStringForOpDetail + singleBase, out );
		}
	}
	
	protected void printOperatorInfoForBindJoinWithUNION ( final PhysicalOpBindJoinWithUNION op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "UNIONBindJoin (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
		if ( lop instanceof LogicalOpTPAdd ) {
			printFederationMember( ((LogicalOpTPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpBGPAdd ) {
			printFederationMember( ((LogicalOpBGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpBGPAdd) lop).getBGP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpGPAdd ) {
			printFederationMember( ((LogicalOpGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpGPAdd) lop).getPattern(), indentLevelStringForOpDetail + singleBase, out );
		}
	}
	
	protected void printOperatorInfoForBindJoinWithVALUES ( final PhysicalOpBindJoinWithVALUES op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "VALUESBindJoin (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
		if ( lop instanceof LogicalOpTPAdd ) {
			printFederationMember( ((LogicalOpTPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpBGPAdd ) {
			printFederationMember( ((LogicalOpBGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpBGPAdd) lop).getBGP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpGPAdd ) {
			printFederationMember( ((LogicalOpGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpGPAdd) lop).getPattern(), indentLevelStringForOpDetail + singleBase, out );
		}	
	}
	
	protected void printOperatorInfoForOpFILTER ( final PhysicalOpFilter op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "filter (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(lop), lop.getID(), out, indentLevelStringForOpDetail );
		out.append( indentLevelStringForOpDetail + singleBase + "  - filterExpressions " + ((LogicalOpFilter) lop).getFilterExpressions().toString() );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForOpGlobalToLocal ( final PhysicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "g2l (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(lop), lop.getID(), out, indentLevelStringForOpDetail );
		out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping " + ((LogicalOpGlobalToLocal) lop).getVocabularyMapping().hashCode() );
		out.append( System.lineSeparator() );	}

	protected void printOperatorInfoForOpHashJoin ( final PhysicalOpHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashJoin (" + op.getID() + ") " );
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpHashRJoin ( final PhysicalOpHashRJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "hashRJoin (" + op.getID() + ") " );
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
	}
	
	protected void printOperatorInfoForOpIndexNestedLoopsJoin ( final PhysicalOpIndexNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "indexNestedLoop (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
		if ( lop instanceof LogicalOpTPAdd ) {
			printFederationMember( ((LogicalOpTPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			printFederationMember( ((LogicalOpTPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpTPAdd) lop).getTP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpBGPAdd ) {
			printFederationMember( ((LogicalOpBGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpBGPAdd) lop).getBGP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpBGPOptAdd ) {
			printFederationMember( ((LogicalOpBGPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpBGPOptAdd) lop).getBGP(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpGPAdd ) {
			printFederationMember( ((LogicalOpGPAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpGPAdd) lop).getPattern(), indentLevelStringForOpDetail + singleBase, out );
		}
		else if (lop instanceof LogicalOpGPOptAdd ) {
			printFederationMember( ((LogicalOpGPOptAdd) lop).getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
			printSPARQLGraphPattern( ((LogicalOpGPOptAdd) lop).getPattern(), indentLevelStringForOpDetail + singleBase, out );
		}
	}

	protected void printOperatorInfoForOpLocalToGlobal ( final PhysicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "l2g (" + op.getID() + ") " );
		final LogicalOperator lop = op.getLogicalOperator();
		printLogicalOperator( nameOfLogicalOp(lop), lop.getID(), out, indentLevelStringForOpDetail );
		out.append( indentLevelStringForOpDetail + singleBase + "  - vocab.mapping " + ((LogicalOpLocalToGlobal) lop).getVocabularyMapping().hashCode() );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForMultiwayUnion ( final PhysicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "multiwayUnion (" + op.getID() + ") " );
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
	}

	protected void printOperatorInfoForOpNaiveNestedLoopsJoin ( final PhysicalOpNaiveNestedLoopsJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "naiveNestedLoop (" + op.getID() + ") " );
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
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
	}
	
	protected void printOperatorInfoForOpSymmetricHashJoin ( final PhysicalOpSymmetricHashJoin op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		out.append( indentLevelString + "symmetricHashJoin (" + op.getID() + ") " );
		printLogicalOperator( nameOfLogicalOp(op.getLogicalOperator()), op.getLogicalOperator().getID(), out, indentLevelStringForOpDetail );
	}
	
	protected String nameOfLogicalOp ( final LogicalOperator lop) {
		if ( lop instanceof LogicalOpUnion ) {
			return "union";
		}
		else if ( lop instanceof LogicalOpMultiwayUnion ) {
			return "mu";
		}
		else if ( lop instanceof LogicalOpTPAdd ) {
			return "tpAdd";
		}
		else if ( lop instanceof LogicalOpTPOptAdd ) {
			return "tpOptAdd";
		}
		else if ( lop instanceof LogicalOpBGPAdd ) {
			return "bgpAdd";
		}
		else if ( lop instanceof LogicalOpBGPOptAdd ) {
			return "bgpOptAdd";
		}
		else if ( lop instanceof LogicalOpGPAdd ) {
			return "gpAdd";
		}
		else if ( lop instanceof LogicalOpGPOptAdd) {
			return "gpOptAdd";
		}
		else if ( lop instanceof LogicalOpFilter ) {
			return "filter";
		}
		else if ( lop instanceof LogicalOpGlobalToLocal ) {
			return "g2l";
		}
		else if ( lop instanceof LogicalOpJoin ) {
			return "join";
		}
		else if ( lop instanceof LogicalOpRightJoin ) {
			return "rightjoin";
		}
		else if ( lop instanceof LogicalOpLocalToGlobal ) {
			return "l2g";
		}
		else if ( lop instanceof LogicalOpRequest ) {
			return "req";
		}
		else {
			throw new IllegalArgumentException( "Unexpected logical operator type: " + lop.getClass().getName() );
		}
	} 
	
	protected void printLogicalOperator ( final String lop, final int id, final PrintStream out, String indentLevelStringForOpDetail ) {
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - lop (" + id + ") " + lop );
		out.append( System.lineSeparator() );
	}
}
