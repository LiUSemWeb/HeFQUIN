package se.liu.ida.hefquin.engine.queryplan.physical;

public class PhysicalPlanWalker {

    public static void walk(final PhysicalPlan plan, final PhysicalPlanVisitor beforeVisitor,
                            final PhysicalPlanVisitor afterVisitor) {
        new PhysicalWalkerVisitor(beforeVisitor, afterVisitor).walk(plan);
    }

    protected static class PhysicalWalkerVisitor{
        protected final PhysicalPlanVisitor beforeVisitor;
        protected final PhysicalPlanVisitor afterVisitor;

        public PhysicalWalkerVisitor(final PhysicalPlanVisitor beforeVisitor,
                                     final PhysicalPlanVisitor afterVisitor) {
            this.beforeVisitor = beforeVisitor;
            this.afterVisitor = afterVisitor;
        }

        public void walk(final PhysicalPlan plan) {
            if ( beforeVisitor != null ) {
                plan.getRootOperator().visit(beforeVisitor);
            }
            for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
                walk( plan.getSubPlan(i) );
            }
            if ( afterVisitor != null ) {
                plan.getRootOperator().visit(afterVisitor);
            }
        }
    }

}
