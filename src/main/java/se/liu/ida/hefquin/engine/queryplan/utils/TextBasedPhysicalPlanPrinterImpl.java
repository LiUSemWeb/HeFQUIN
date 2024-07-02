package se.liu.ida.hefquin.engine.queryplan.utils;

import java.io.PrintStream;

import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;
import se.liu.ida.hefquin.engine.utils.IndentingPrintStream;

public class TextBasedPhysicalPlanPrinterImpl implements PhysicalPlanPrinter
{
	@Override
	public void print( final PhysicalPlan plan, final PrintStream out ) {
		final IndentingPrintStream iOut = new IndentingPrintStream(out);
		final PhysicalPlanVisitor beforeVisitor = new MyBeforeVisitor(iOut);
		final PhysicalPlanVisitor afterVisitor = new MyAfterVisitor(iOut);

		PhysicalPlanWalker.walk(plan, beforeVisitor, afterVisitor);
		iOut.flush();
	}


	protected class MyBeforeVisitor implements PhysicalPlanVisitor {

		protected final IndentingPrintStream out;

		public MyBeforeVisitor( final IndentingPrintStream out ) {
			this.out = out;
		}

		@Override
		public void visit( final PhysicalOpRequest<?, ?> op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append( System.lineSeparator() );
		}

		@Override
		public void visit( final PhysicalOpBindJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithVALUES op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithFILTER op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpBindJoinWithUNION op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpIndexNestedLoopsJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpNaiveNestedLoopsJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpParallelMultiLeftJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append( System.lineSeparator() );
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpHashJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpSymmetricHashJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpHashRJoin op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append( System.lineSeparator() );
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpBinaryUnion op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpMultiwayUnion op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append( System.lineSeparator() );
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpFilter op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpLocalToGlobal op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpGlobalToLocal op ) {
			out.appendIndentation();
			out.append( op.toString() );
			out.append(System.lineSeparator());
			out.increaseIndentationLevel();
		}
	}

	protected class MyAfterVisitor implements PhysicalPlanVisitor {

		protected final IndentingPrintStream out;

		public MyAfterVisitor( final IndentingPrintStream out ) {
			this.out = out;
		}

		@Override
		public void visit(final PhysicalOpRequest<?, ?> op) {
			// nothing to do here
		}

		@Override
		public void visit(final PhysicalOpBindJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpBindJoinWithVALUES op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpBindJoinWithFILTER op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpBindJoinWithUNION physicalOpBindJoinWithUNION) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpNaiveNestedLoopsJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpIndexNestedLoopsJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpParallelMultiLeftJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpHashJoin op ) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpSymmetricHashJoin op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit( final PhysicalOpHashRJoin op ) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpBinaryUnion op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpMultiwayUnion op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpFilter op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpLocalToGlobal op) {
			out.decreaseIndentationLevel();
		}

		@Override
		public void visit(final PhysicalOpGlobalToLocal op) {
			out.decreaseIndentationLevel();
		}
	}

}
