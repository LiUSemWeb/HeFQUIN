package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayLeftJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

public class TextBasedLogicalPlanPrinterImpl extends BaseForTextBasedPlanPrinters implements LogicalPlanPrinter
{
	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
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
	public void planWalk( final LogicalPlan plan, final int planNumber, final int planLevel, final int numberOfSiblings, final PrintStream out, final String rootOpIndentString ) {
		final LogicalOperator rootOp = plan.getRootOperator();
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, plan.numberOfSubPlans(), indentLevelString);
		if ( rootOp instanceof LogicalOpBGPAdd addOp ) {
			printOperatorInfoForBGPAdd( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpBGPOptAdd addOp ) {
			printOperatorInfoForBGPOptAdd( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpBind bindOp) {
			printOperatorInfoForBind( bindOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpFilter filterOp ) {
			printOperatorInfoForFilter( filterOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGlobalToLocal g2lOp ) {
			printOperatorInfoForGlobalToLocal( g2lOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGPAdd addOp ) {
			printOperatorInfoForGPAdd( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGPOptAdd addOp ) {
			printOperatorInfoForGPOptAdd( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpJoin joinOp ) {
			printOperatorInfoForJoin( joinOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpLocalToGlobal l2gOp ) {
			printOperatorInfoForLocalToGlobal( l2gOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpMultiwayJoin mjOp ) {
			printOperatorInfoForMultiwayJoin( mjOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpMultiwayLeftJoin mljOp ) {
			printOperatorInfoForMultiwayLeftJoin( mljOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpMultiwayUnion muOp ) {
			printOperatorInfoMultiwayUnion( muOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpRequest reqOp ) {
			printOperatorInfoForRequest( reqOp, out, indentLevelString, indentLevelStringForOpDetail );			
		}
		else if ( rootOp instanceof LogicalOpRightJoin rightJoinOp) {
			printOperatorInfoForRightJoin( rightJoinOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpTPAdd addOp ) {
			printOperatorInfoForTPAdd( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpTPOptAdd addOp ) {
			printOperatorInfoForTPOptAdd( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpUnion unionOp ) {
			printOperatorInfoForUnion( unionOp, out, indentLevelString );
		}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out, indentLevelString );
		}
	}
	
	protected void printOperatorInfoForBGPAdd ( final LogicalOpBGPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBGPOptAdd ( final LogicalOpBGPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForBind( final LogicalOpBind op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - expression (" + op.getBindExpressions().toString() +  ") " );
	}
	
	protected void printOperatorInfoForFilter( final LogicalOpFilter op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - expression (" + op.getFilterExpressions().toString() +  ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForGlobalToLocal( final LogicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + "  - vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForGPAdd ( final LogicalOpGPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForGPOptAdd ( final LogicalOpGPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForJoin( final LogicalOpJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForLocalToGlobal( final LogicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + "  - vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForMultiwayJoin( final LogicalOpMultiwayJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForMultiwayLeftJoin( final LogicalOpMultiwayLeftJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoMultiwayUnion( final LogicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForRequest( final LogicalOpRequest op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final DataRetrievalRequest req = op.getRequest();
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") " + req.toString() );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForRightJoin( final LogicalOpRightJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForTPAdd ( final LogicalOpTPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForTPOptAdd ( final LogicalOpTPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}
	
	protected void printOperatorInfoForUnion( final LogicalOpUnion op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
	}
	
}
