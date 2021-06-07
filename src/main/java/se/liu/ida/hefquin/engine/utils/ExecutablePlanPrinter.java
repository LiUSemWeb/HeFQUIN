package se.liu.ida.hefquin.engine.utils;

import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanVisitor;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutablePlanWalker;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.*;

public class ExecutablePlanPrinter extends PlanPrinter{

    protected final ExecutablePlanPrinterBeforeVisitor beforeVisitor = new ExecutablePlanPrinterBeforeVisitor();
    protected final ExecutablePlanPrinterAfterVisitor afterVisitor = new ExecutablePlanPrinterAfterVisitor();

    static public String print(final ExecutablePlan plan) {
        final ExecutablePlanPrinter printer = new ExecutablePlanPrinter();
        ExecutablePlanWalker.walk(plan, printer.beforeVisitor, printer.afterVisitor);
        return printer.getString();
    }

    private class ExecutablePlanPrinterBeforeVisitor implements ExecutablePlanVisitor {

        @Override
        public void visit(final ExecOpRequestBRTPF op) {
            addTabs();
            builder.append("> BRTPFRequest ");
            builder.append(System.lineSeparator());
        }

        @Override
        public void visit(final ExecOpRequestSPARQL op) {
            addTabs();
            builder.append("> SPARQLRequest");
            builder.append(System.lineSeparator());
        }

        @Override
        public void visit(final ExecOpRequestTPFatBRTPFServer op) {
            addTabs();
            builder.append("> TPFRequest @BRTPFServer");
            builder.append(System.lineSeparator());
        }

        @Override
        public void visit(final ExecOpRequestTPFatTPFServer op) {
            addTabs();
            builder.append("> TPFRequest @TPFServer");
            builder.append(System.lineSeparator());
        }

        @Override
        public void visit(final ExecOpBindJoinBRTPF op) {
            addTabs();
            builder.append("> BRTPFBindJoin");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpBindJoinSPARQLwithFILTER op) {
            addTabs();
            builder.append("> SPARQLBindJoin w/FILTER");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpBindJoinSPARQLwithUNION op) {
            addTabs();
            builder.append("> SPARQLBindJoin w/UNION");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpBindJoinSPARQLwithVALUES op) {
            addTabs();
            builder.append("> SPARQLBindJoin w/VALUES");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpNaiveNestedLoopsJoin op) {
            addTabs();
            builder.append("> NaiveNestedLoopsJoin");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpIndexNestedLoopsJoinTPF op) {
            addTabs();
            builder.append("> IndexNestedLoopsJoin");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpIndexNestedLoopsJoinBRTPF op) {
            addTabs();
            builder.append("> IndexNestedLoopsJoinBRTPF");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpIndexNestedLoopsJoinSPARQL op) {
            addTabs();
            builder.append("> IndexNestedLoopsJoinSPARQL");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpHashJoin op) {
            addTabs();
            builder.append("> HashJoin");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpSymmetricHashJoin op) {
            addTabs();
            builder.append("> SymmetricHashJoin");
            builder.append(System.lineSeparator());
            indentLevel++;
        }

        @Override
        public void visit(final ExecOpBinaryUnion op) {
            addTabs();
            builder.append("> BinaryUnion");
            builder.append(System.lineSeparator());
            indentLevel++;
        }
    }

    private class ExecutablePlanPrinterAfterVisitor implements ExecutablePlanVisitor{

        @Override
        public void visit(final ExecOpRequestBRTPF op) {
            // nothing to do here
        }

        @Override
        public void visit(final ExecOpRequestSPARQL op) {
            // nothing to do here
        }

        @Override
        public void visit(final ExecOpRequestTPFatBRTPFServer op) {
            // nothing to do here
        }

        @Override
        public void visit(final ExecOpRequestTPFatTPFServer op) {
            // nothing to do here
        }

        @Override
        public void visit(final ExecOpBindJoinBRTPF op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpBindJoinSPARQLwithFILTER op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpBindJoinSPARQLwithUNION op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpBindJoinSPARQLwithVALUES op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpNaiveNestedLoopsJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpIndexNestedLoopsJoinTPF op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpIndexNestedLoopsJoinBRTPF op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpIndexNestedLoopsJoinSPARQL op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpHashJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpSymmetricHashJoin op) {
            indentLevel--;
        }

        @Override
        public void visit(final ExecOpBinaryUnion op) {
            indentLevel--;
        }
    }
}
