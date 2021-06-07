package se.liu.ida.hefquin.engine.queryplan.executable;

import se.liu.ida.hefquin.engine.queryplan.ExecutablePlan;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased.ResultElementIterator;

public class ExecutablePlanWalker {

    public static void walk(final ExecutablePlan plan, final ExecutablePlanVisitor beforeVisitor,
                            final ExecutablePlanVisitor afterVisitor) {
        new ExecutableWalkerVisitor(beforeVisitor, afterVisitor).walk(plan.getIterator());
    }

    private static class ExecutableWalkerVisitor {
        private final ExecutablePlanVisitor beforeVisitor;
        private final ExecutablePlanVisitor afterVisitor;

        public ExecutableWalkerVisitor(final ExecutablePlanVisitor beforeVisitor,
                                       final ExecutablePlanVisitor afterVisitor) {
            this.beforeVisitor = beforeVisitor;
            this.afterVisitor = afterVisitor;
        }

        public void walk(final ResultElementIterator iter) {
            if (beforeVisitor != null) {
                iter.getOp().visit(beforeVisitor);
            }
            for (int i = 0; i < iter.getArity(); i++) {
                walk(iter.getSubIterator(i));
            }
            if (afterVisitor != null) {
                iter.getOp().visit(afterVisitor);
            }
        }
    }
}
