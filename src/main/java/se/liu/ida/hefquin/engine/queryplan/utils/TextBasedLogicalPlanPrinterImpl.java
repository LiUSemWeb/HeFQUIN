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
	public void planWalk( final LogicalPlan plan,
	                      final int planNumber,
	                      final int planLevel,
	                      final int numberOfSiblings,
	                      final PrintStream out,
	                      final String rootOpIndentString ) {
		final String indentLevelString = getIndentLevelString(planNumber, planLevel, numberOfSiblings, rootOpIndentString);
		final String indentLevelStringForOpDetail = getIndentLevelStringForDetail(planNumber, planLevel, numberOfSiblings, plan.numberOfSubPlans(), indentLevelString);

		final LogicalOperator rootOp = plan.getRootOperator();
		if ( rootOp instanceof LogicalOpBGPAdd addOp ) {
			printOp( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpBGPOptAdd addOp ) {
			printOp( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpBind bindOp) {
			printOp( bindOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpFilter filterOp ) {
			printOp( filterOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGlobalToLocal g2lOp ) {
			printOp( g2lOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGPAdd addOp ) {
			printOp( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpGPOptAdd addOp ) {
			printOp( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpJoin joinOp ) {
			printOp( joinOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpLocalToGlobal l2gOp ) {
			printOp( l2gOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpMultiwayJoin mjOp ) {
			printOp( mjOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpMultiwayLeftJoin mljOp ) {
			printOp( mljOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpMultiwayUnion muOp ) {
			printOp( muOp, out, indentLevelString );
		}
		else if (rootOp instanceof LogicalOpRequest reqOp ) {
			printOp( reqOp, out, indentLevelString, indentLevelStringForOpDetail );			
		}
		else if ( rootOp instanceof LogicalOpRightJoin rightJoinOp) {
			printOp( rightJoinOp, out, indentLevelString );
		}
		else if ( rootOp instanceof LogicalOpTPAdd addOp ) {
			printOp( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpTPOptAdd addOp ) {
			printOp( addOp, out, indentLevelString, indentLevelStringForOpDetail );
		}
		else if ( rootOp instanceof LogicalOpUnion unionOp ) {
			printOp( unionOp, out, indentLevelString );
		}
		else {
			throw new IllegalArgumentException( "Unexpected operator type: " + rootOp.getClass().getName() );
		}

		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel+1, plan.numberOfSubPlans(), out, indentLevelString );
		}
	}

	protected void printOp( final LogicalOpBGPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpBGPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getBGP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpBind op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - expression (" + op.getBindExpressions().toString() +  ") " );
	}

	protected void printOp( final LogicalOpFilter op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + singleBase + "  - expression (" + op.getFilterExpressions().toString() +  ") " );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpGlobalToLocal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + "  - vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpGPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpGPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getPattern(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpLocalToGlobal op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail + "  - vocab.mapping (" + op.getVocabularyMapping().hashCode() +  ") " );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpMultiwayJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpMultiwayLeftJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpMultiwayUnion op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpRequest<?,?> op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		final DataRetrievalRequest req = op.getRequest();
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail, out );
		out.append( indentLevelStringForOpDetail + "  - pattern (" + req.hashCode() +  ") " + req.toString() );
		out.append( System.lineSeparator() );
		out.append( indentLevelStringForOpDetail );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpRightJoin op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpTPAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpTPOptAdd op, final PrintStream out, final String indentLevelString, final String indentLevelStringForOpDetail ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
		out.append( System.lineSeparator() );
		printFederationMember( op.getFederationMember(), indentLevelStringForOpDetail + singleBase, out );
		printSPARQLGraphPattern( op.getTP(), indentLevelStringForOpDetail + singleBase, out );
		out.append( indentLevelStringForOpDetail + singleBase );
		out.append( System.lineSeparator() );
	}

	protected void printOp( final LogicalOpUnion op, final PrintStream out, final String indentLevelString ) {
		printLogicalOperatorBase( "", op, out, indentLevelString );
	}

}
