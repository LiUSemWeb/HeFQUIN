package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

/**
 * Applies a {@link LogicalPlanVisitor} to
 * a given {@link LogicalPlan} recursively
 * in a depth-first order.
 */
public class LogicalPlanWalker
{
	public static void walk( final LogicalPlan plan, final LogicalPlanVisitor beforeVisitor,
							 final LogicalPlanVisitor afterVisitor ) {
		new LogicalWalkerVisitor(beforeVisitor, afterVisitor).walk(plan);
	}

	protected static class LogicalWalkerVisitor {
		protected final LogicalPlanVisitor beforeVisitor;
		protected final LogicalPlanVisitor afterVisitor;

		public LogicalWalkerVisitor( final LogicalPlanVisitor beforeVisitor,
							  final LogicalPlanVisitor afterVisitor ) {
			this.beforeVisitor = beforeVisitor;
			this.afterVisitor = afterVisitor;
		}

		public void walk( final LogicalPlan plan ) {
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
	} // end of class WalkerVisitor
}
