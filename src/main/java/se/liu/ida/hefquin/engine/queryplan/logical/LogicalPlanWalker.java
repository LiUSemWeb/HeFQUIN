package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;

/**
 * Applies a {@link LogicalPlanVisitor} to
 * a given {@link LogicalPlan} recursively
 * in a depth-first order.
 */
public class LogicalPlanWalker
{
	public static void walk( final LogicalPlan plan, final LogicalPlanVisitor visitor ) {
		new WalkerVisitor(visitor).walk(plan);
	}

	protected static class WalkerVisitor {
		protected final LogicalPlanVisitor visitor;

		public WalkerVisitor( final LogicalPlanVisitor visitor ) {
			assert visitor != null;
			this.visitor = visitor;
		}

		public void walk( final LogicalPlan plan ) {
			for (int i = 0; i < plan.numberOfSubPlans(); ++i ) {
				walk( plan.getSubPlan(i) );
			}
			plan.getRootOperator().visit(visitor);
		}

	} // end of class WalkerVisitor 

}
