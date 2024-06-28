package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;
import se.liu.ida.hefquin.engine.utils.IndentingPrintStream;

public class TextBasedLogicalPlanPrinterImpl2 implements LogicalPlanPrinter
{
	
	@Override
	public void print( final LogicalPlan plan, final PrintStream out ) {
		final IndentingPrintStream iOut = new IndentingPrintStream(out);
		final LogicalPlanVisitor beforeVisitor = new MyBeforeVisitor(iOut);
		final LogicalPlanVisitor afterVisitor = new MyAfterVisitor(iOut);

		LogicalPlanWalker.walk(plan, beforeVisitor, afterVisitor);
		iOut.flush();
		System.out.println("----------- re-impl -----------");
		planWalk(plan, 0, 0, 0);
	}
	
	public String getIndentLevelString(int indentLevel) {
		String indentLevelString = "";
		for ( int i = 0; i < indentLevel; i++ ) {
			indentLevelString += "   ";
		}
		return indentLevelString;
	}
	
	public String getIndentLevelString2(int planLevel, int planNumber, int planFlag) {
		String indentLevelString = "";
		for ( int i = 0; i < planLevel; i++ ) {
			indentLevelString += "   ";
		}
		return indentLevelString;
	}
	
	public void planWalk( final LogicalPlan plan, int planNumber, int planLevel, int planFlag ) {
		System.out.println(getIndentLevelString(planLevel) + plan.getRootOperator().toString() +" "+planLevel + " " + planNumber);
		
		planLevel += 1;
		for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
			planWalk( plan.getSubPlan(i), i, planLevel, i);
		}
		
	}


	protected  class MyBeforeVisitor implements LogicalPlanVisitor {

		protected final IndentingPrintStream out;
		
		protected boolean flagOne = false; 
		
		protected boolean flagTwo = false;

		public MyBeforeVisitor( final IndentingPrintStream out ) {
			this.out = out;
		}

		@Override
		public void visit(final LogicalOpRequest<?, ?> op) {
			//out.appendIndentation();
			out.appendLevelIndentation(flagOne, flagTwo);
			out.append( op.toString() );
			out.append(System.lineSeparator());
			flagOne = false;
		}

		@Override
		public void visit(final LogicalOpTPAdd op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpBGPAdd op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpGPAdd op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpTPOptAdd op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpBGPOptAdd op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpGPOptAdd op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpJoin op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpRightJoin op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpUnion op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpMultiwayJoin op) {
			//out.appendIndentation();
			out.appendLevelIndentation(flagOne, flagTwo);
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
			flagOne = true;
		}

		@Override
		public void visit(final LogicalOpMultiwayLeftJoin op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append( System.lineSeparator() );
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpMultiwayUnion op) {
			//out.appendIndentation();
			out.appendLevelIndentation(flagOne, flagTwo);
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
			flagOne = true;
		}
		
		@Override
		public void visit(final LogicalOpFilter op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpBind op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}
		
		@Override
		public void visit(final LogicalOpLocalToGlobal op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}
		
		@Override
		public void visit(final LogicalOpGlobalToLocal op) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}
	}

	protected class MyAfterVisitor implements LogicalPlanVisitor {

		protected final IndentingPrintStream out;

		public MyAfterVisitor( final IndentingPrintStream out ) {
			this.out = out;
		}

		@Override
		public void visit(final LogicalOpRequest<?, ?> op) {
			//nothing to do here
		}

		@Override
		public void visit(final LogicalOpTPAdd op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpBGPAdd op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpGPAdd op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpTPOptAdd op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpBGPOptAdd op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpGPOptAdd op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpRightJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpUnion op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpMultiwayJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpMultiwayLeftJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpMultiwayUnion op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpFilter op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpBind op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpLocalToGlobal op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final LogicalOpGlobalToLocal op) {
			out.decreaseIndentationLevel();
		}
	}

}
