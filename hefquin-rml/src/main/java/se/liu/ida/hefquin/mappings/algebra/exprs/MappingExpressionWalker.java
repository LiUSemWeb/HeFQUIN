package se.liu.ida.hefquin.mappings.algebra.exprs;

import se.liu.ida.hefquin.mappings.algebra.MappingOperatorVisitor;

public class MappingExpressionWalker
{
	public static void walk( final MappingExpression expr,
	                         final MappingOperatorVisitor beforeVisitor,
	                         final MappingOperatorVisitor afterVisitor ) {
		new Worker(beforeVisitor, afterVisitor).walk(expr);
	}

	protected static class Worker {
		protected final MappingOperatorVisitor beforeVisitor;
		protected final MappingOperatorVisitor afterVisitor;

		public Worker( final MappingOperatorVisitor beforeVisitor,
		               final MappingOperatorVisitor afterVisitor ) {
			this.beforeVisitor = beforeVisitor;
			this.afterVisitor = afterVisitor;
		}

		public void walk( final MappingExpression expr ) {
			if ( beforeVisitor != null ) {
				expr.getRootOperator().visit(beforeVisitor);
			}

			for ( int i = 0; i < expr.numberOfSubExpressions(); ++i ) {
				walk( expr.getSubExpression(i) );
			}

			if ( afterVisitor != null ) {
				expr.getRootOperator().visit(afterVisitor);
			}
		}
	}
}
