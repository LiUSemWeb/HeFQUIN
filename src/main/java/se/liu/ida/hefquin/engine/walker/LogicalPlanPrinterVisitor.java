package se.liu.ida.hefquin.engine.walker;

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

public class LogicalPlanPrinterVisitor implements LogicalPlanVisitor{
	
	protected int indentLevel = 0;
	protected StringBuilder builder = new StringBuilder();
	
	static public String print( final LogicalPlan plan ) {
		final LogicalPlanPrinterVisitor printer = new LogicalPlanPrinterVisitor();
		LogicalPlanWalker.walkTopDown(plan, printer);
		return printer.getString();
	}

	private String getString() {
		return builder.toString();
	}

	@Override
	public void visit(LogicalOpRequest<?, ?> op) {
		addTabs();
		builder.append("> req");
		//builder.append(op.getFederationMember().toString());
		//builder.append(" ");
		//builder.append(op.getRequest().toString());
		builder.append("\n");
		if (indentLevel > 1)
			indentLevel--;
	}

	@Override
	public void visit(LogicalOpTPAdd op) {
		addTabs();
		builder.append("> tpAdd ");
		//builder.append(op.getFederationMember().toString());
		//builder.append(" ");
		builder.append(op.getTP().toString());
		//builder.append(")");
		builder.append("\n");
		indentLevel++;
	}

	@Override
	public void visit(LogicalOpBGPAdd op) {
		addTabs();
		builder.append("> bgpAdd ");
		//builder.append(op.getFederationMember().toString());
		//builder.append(" ");
		indentLevel++;
		builder.append("> bgp");
		indentLevel++;
		for(final TriplePattern tp : op.getBGP().getTriplePatterns()) {
			builder.append("> tp ");
			builder.append(tp.toString());
			builder.append("\n");
		}
		indentLevel--;
	}

	@Override
	public void visit(LogicalOpJoin op) {
		addTabs();
		builder.append("> join\n");
		indentLevel++;
	}

	@Override
	public void visit(LogicalOpUnion op) {
		addTabs();
		builder.append("> union\n");
		indentLevel++;
	}

	@Override
	public void visit(LogicalOpMultiwayJoin op) {
		addTabs();
		builder.append("> mj ");
		indentLevel++;
	}

	@Override
	public void visit(LogicalOpMultiwayUnion op) {
		addTabs();
		builder.append("mu ");
		indentLevel++;
	}
	
	private void addTabs() {
		for( int i=0; i<indentLevel; i++)
			builder.append("  ");
	}


}
