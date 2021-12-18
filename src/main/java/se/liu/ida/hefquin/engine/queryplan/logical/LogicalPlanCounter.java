package se.liu.ida.hefquin.engine.queryplan.logical;

import se.liu.ida.hefquin.engine.queryplan.LogicalPlan;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalPlanWalker.LogicalWalkerVisitor;

/**
 * Applies a {@link LogicalPlanVisitor} to
 * a given {@link LogicalPlan} recursively
 * in a depth-first order.
 * Unlike LogicalPlanWalker, also counts the number of subplans.
 */
public class LogicalPlanCounter
{
	public static void walk( final LogicalPlan plan, final LogicalPlanVisitor beforeVisitor,
							 final LogicalPlanVisitor afterVisitor ) {
		new LogicalCounterVisitor(beforeVisitor, afterVisitor).walk(plan);
	}

	protected static class LogicalCounterVisitor extends LogicalWalkerVisitor {
		protected int planAmount = 0;
		
		public LogicalCounterVisitor( final LogicalPlanVisitor beforeVisitor,
							  final LogicalPlanVisitor afterVisitor ) {
			super(beforeVisitor,afterVisitor);
		}
		
		public int countPlans( final LogicalPlan plan ) {
			planAmount = 0;
			walk(plan);
			return planAmount;
		}

		public void walk( final LogicalPlan plan ) {
			if ( beforeVisitor != null ) {
				plan.getRootOperator().visit(beforeVisitor);
			}
			for ( int i = 0; i < plan.numberOfSubPlans(); ++i ) {
				planAmount++;
				walk( plan.getSubPlan(i) );
			}
			if ( afterVisitor != null ) {
				plan.getRootOperator().visit(afterVisitor);
			}
		}
	}
}