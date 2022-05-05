package se.liu.ida.hefquin.engine.queryplan.utils;

import se.liu.ida.hefquin.engine.queryplan.PhysicalPlan;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.physical.PhysicalPlanWalker;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.*;

public class PhysicalPlanPrinter extends PlanPrinter{

    protected final PhysicalPlanPrinterBeforeVisitor beforeVisitor = new PhysicalPlanPrinterBeforeVisitor();
    protected final PhysicalPlanPrinterAfterVisitor afterVisitor = new PhysicalPlanPrinterAfterVisitor();

    static public String print(final PhysicalPlan plan ) {
        final PhysicalPlanPrinter printer = new PhysicalPlanPrinter();
        PhysicalPlanWalker.walk(plan, printer.beforeVisitor, printer.afterVisitor);
        return printer.getString();
    }

    private class PhysicalPlanPrinterBeforeVisitor implements PhysicalPlanVisitor {
        @Override
        public void visit( final PhysicalOpRequest<?, ?> op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append( System.lineSeparator() );
        }

        @Override
        public void visit( final PhysicalOpRequestWithTranslation<?, ?> op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append( System.lineSeparator() );
        }

        @Override
        public void visit( final PhysicalOpBindJoin op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBindJoinWithVALUES op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBindJoinWithFILTER op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBindJoinWithUNION op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpIndexNestedLoopsJoin op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpNaiveNestedLoopsJoin op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpHashJoin op) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpSymmetricHashJoin op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpBinaryUnion op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit( final PhysicalOpFilter op ) {
            addTabs();
            builder.append( op.toString() );
            builder.append(System.lineSeparator());
            indentLevel++;
        }

    }

    private class PhysicalPlanPrinterAfterVisitor implements PhysicalPlanVisitor {
        @Override
        public void visit(final PhysicalOpRequest<?, ?> op) {
            // nothing to do here
        }

        @Override
        public void visit(final PhysicalOpRequestWithTranslation<?, ?> op) {
            // nothing to do here
        }

        @Override
        public void visit(final PhysicalOpBindJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBindJoinWithVALUES op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBindJoinWithFILTER op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBindJoinWithUNION physicalOpBindJoinWithUNION) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpNaiveNestedLoopsJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpIndexNestedLoopsJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(PhysicalOpHashJoin physicalOpHashJoin) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpSymmetricHashJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpBinaryUnion op) {
            indentLevel--;
        }

        @Override
        public void visit(final PhysicalOpFilter op) {
            indentLevel--;
        }
    }
}
