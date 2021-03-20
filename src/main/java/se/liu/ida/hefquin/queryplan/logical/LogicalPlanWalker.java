package se.liu.ida.hefquin.queryplan.logical;

import se.liu.ida.hefquin.queryplan.LogicalOperator;
import se.liu.ida.hefquin.queryplan.LogicalPlan;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpBGPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpMultiwayJoin;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.queryplan.logical.impl.LogicalOpUnion;

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

	protected static class WalkerVisitor extends LogicalPlanVisitorBase {
		protected final LogicalPlanVisitor visitor;

		public WalkerVisitor( final LogicalPlanVisitor visitor ) {
			assert visitor != null;
			this.visitor = visitor;
		}

		public void walk( final LogicalPlan plan ) {
			plan.getRootOperator().visit(this);
		}

		@Override
		public void visit( final LogicalOpRequest<?,?> op ) { visit0(op); }

		@Override
		public void visit( final LogicalOpTPAdd op ) { visit1(op); }

		@Override
		public void visit( final LogicalOpBGPAdd op ) { visit1(op); }

		@Override
		public void visit( final LogicalOpJoin op ) { visit2(op); }

		@Override
		public void visit( final LogicalOpUnion op ) { visit2(op); }

		@Override
		public void visit( final LogicalOpMultiwayJoin op ) { visitN(op); }

		@Override
		public void visit( final LogicalOpMultiwayUnion op ) { visitN(op); }

		protected void visit0( final LogicalOperator op ) {
			op.visit(visitor);
		}

		protected void visit1( final UnaryLogicalOp op ) {
			op.getChildOp().visit(this);
			op.visit(visitor);
		}

		protected void visit2( final BinaryLogicalOp op ) {
			op.getChildOp1().visit(this);
			op.getChildOp2().visit(this);
			op.visit(visitor);
		}

		protected void visitN( final NaryLogicalOp op ) {
			for( final LogicalOperator child : op.getChildren() ) {
				child.visit(this);
			}

			op.visit(visitor);
		}

	} // end of class WalkerVisitor 

}
