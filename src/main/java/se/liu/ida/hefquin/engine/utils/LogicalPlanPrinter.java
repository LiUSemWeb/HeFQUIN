package se.liu.ida.hefquin.engine.utils;

import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;

public class LogicalPlanPrinter {
	
	protected int indentLevel = 0;
	protected StringBuilder builder = new StringBuilder();
	protected final LogicalPlanPrinterBeforeVisitor beforeVisitor = new LogicalPlanPrinterBeforeVisitor();
	protected final LogicalPlanPrinterAfterVisitor afterVisitor = new LogicalPlanPrinterAfterVisitor();
	
	static public String print( final LogicalPlan plan ) {
		final LogicalPlanPrinter printer = new LogicalPlanPrinter();
		LogicalPlanWalker.walk(plan, printer.beforeVisitor, printer.afterVisitor);
		return printer.getString();
	}

	private String getString() {
		return builder.toString();
	}

	private class LogicalPlanPrinterBeforeVisitor implements LogicalPlanVisitor {

		@Override
		public void visit(final LogicalOpRequest<?, ?> op) {
			addTabs();
			builder.append("> req");
			//builder.append(op.getFederationMember().toString());
			//builder.append(" ");
			//builder.append(op.getRequest().toString());
			builder.append(System.lineSeparator());
		}

		@Override
		public void visit(final LogicalOpTPAdd op) {
			addTabs();
			builder.append("> tpAdd ");
			//builder.append(op.getFederationMember().toString());
			//builder.append(" ");
			builder.append(op.getTP().toString());
			//builder.append(")");
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpBGPAdd op) {
			addTabs();
			builder.append("> bgpAdd ");
			//builder.append(op.getFederationMember().toString());
			//builder.append(" ");
			indentLevel++;
			builder.append(System.lineSeparator());
			addTabs();
			builder.append("( bgp");
			indentLevel++;
			for (final TriplePattern tp : op.getBGP().getTriplePatterns()) {
				addTabs();
				builder.append("( tp ");
				builder.append(tp.toString());
				builder.append(")");
				builder.append(System.lineSeparator());
			}
			builder.append(")");
			builder.append(System.lineSeparator());
			indentLevel--;
		}

		@Override
		public void visit(final LogicalOpJoin op) {
			addTabs();
			builder.append("> join");
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpUnion op) {
			addTabs();
			builder.append("> union");
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpMultiwayJoin op) {
			addTabs();
			builder.append("> mj ");
			builder.append(System.lineSeparator());
			indentLevel++;
		}

		@Override
		public void visit(final LogicalOpMultiwayUnion op) {
			addTabs();
			builder.append("> mu ");
			builder.append(System.lineSeparator());
			indentLevel++;
		}
	}

	private class LogicalPlanPrinterAfterVisitor implements LogicalPlanVisitor {

		@Override
		public void visit(LogicalOpRequest<?, ?> op) {
			//nothing to do here
		}

		@Override
		public void visit(LogicalOpTPAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(LogicalOpBGPAdd op) {
			indentLevel--;
		}

		@Override
		public void visit(LogicalOpJoin op) {
			indentLevel--;
		}

		@Override
		public void visit(LogicalOpUnion op) {
			indentLevel--;
		}

		@Override
		public void visit(LogicalOpMultiwayJoin op) {
			indentLevel--;
		}

		@Override
		public void visit(LogicalOpMultiwayUnion op) {
			indentLevel--;
		}
	}

	protected void addTabs() {
		for (int i = 0; i < indentLevel; i++)
			builder.append("  ");
	}
}
