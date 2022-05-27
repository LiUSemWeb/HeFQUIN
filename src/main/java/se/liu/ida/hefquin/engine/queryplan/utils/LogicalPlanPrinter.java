package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

public class LogicalPlanPrinter extends PlanPrinter{

	protected final LogicalPlanPrinterBeforeVisitor beforeVisitor = new LogicalPlanPrinterBeforeVisitor();
	protected final LogicalPlanPrinterAfterVisitor afterVisitor = new LogicalPlanPrinterAfterVisitor();
	
	static public String print( final LogicalPlan plan ) {
		final LogicalPlanPrinter printer = new LogicalPlanPrinter();
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
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpJoin op) {
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
		public void visit(final LogicalOpLocalToGlobal op) {
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
		public void visit(final LogicalOpJoin op) {
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
		public void visit(final LogicalOpMultiwayUnion op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpFilter op) {
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpLocalToGlobal op) {
			indentLevel--;
		}
	}
}
