package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.*;

public class TextBasedLogicalPlanPrinterImpl extends TextBasedPlanPrinterBase
{
	protected final LogicalPlanPrinterBeforeVisitor beforeVisitor = new LogicalPlanPrinterBeforeVisitor();
	protected final LogicalPlanPrinterAfterVisitor afterVisitor = new LogicalPlanPrinterAfterVisitor();
	
	static public String print( final LogicalPlan plan ) {
		final TextBasedLogicalPlanPrinterImpl printer = new TextBasedLogicalPlanPrinterImpl();
		LogicalPlanWalker.walk(plan, printer.beforeVisitor, printer.afterVisitor);
		return printer.getString();
	}

	private class LogicalPlanPrinterBeforeVisitor implements LogicalPlanVisitor {

		@Override
		public void visit(final LogicalOpRequest<?, ?> op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
		}

		@Override
		public void visit(final LogicalOpTPAdd op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpBGPAdd op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpGPAdd op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpTPOptAdd op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpBGPOptAdd op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpGPOptAdd op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpJoin op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpRightJoin op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpUnion op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpMultiwayJoin op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpMultiwayLeftJoin op) {
			addTabs();
			builder.append( op.toString() );
			builder.append( System.lineSeparator() );
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpMultiwayUnion op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}
		
		@Override
		public void visit(final LogicalOpFilter op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpBind op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}
		
		@Override
		public void visit(final LogicalOpLocalToGlobal op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}
		
		@Override
		public void visit(final LogicalOpGlobalToLocal op) {
			addTabs();
			builder.append( op.toString() );
			builder.append(System.lineSeparator());
			indentLevel++;
		}
	}

	private class LogicalPlanPrinterAfterVisitor implements LogicalPlanVisitor {

		@Override
		public void visit(final LogicalOpRequest<?, ?> op) {
			//nothing to do here
		}

		@Override
		public void visit(final LogicalOpTPAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpBGPAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpGPAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpTPOptAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpBGPOptAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpGPOptAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpJoin op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpRightJoin op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpUnion op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpMultiwayJoin op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpMultiwayLeftJoin op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpMultiwayUnion op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpFilter op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpBind op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpLocalToGlobal op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpGlobalToLocal op) {
			indentLevel--;
		}
	}
}
