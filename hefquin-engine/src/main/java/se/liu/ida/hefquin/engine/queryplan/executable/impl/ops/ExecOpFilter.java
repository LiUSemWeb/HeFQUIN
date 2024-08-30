package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.FilteringIteratorForSolMaps_ExprList;

public class ExecOpFilter extends UnaryExecutableOpBaseWithIterator
{
	protected final ExprList filterExpressions;

	public ExecOpFilter( final ExprList filterExpressions, final boolean collectExceptions ) {
		super(collectExceptions);

		assert filterExpressions != null;
		assert ! filterExpressions.isEmpty();

		this.filterExpressions = filterExpressions;
	}

	public ExecOpFilter( final Expr filterExpression, final boolean collectExceptions ) {
		super(collectExceptions);

		assert filterExpression != null;

		this.filterExpressions = new ExprList(filterExpression);
	}

	@Override
	protected Iterator<SolutionMapping> createInputToOutputIterator( final Iterable<SolutionMapping> input ) {
		return new FilteringIteratorForSolMaps_ExprList(input, filterExpressions);
	}

}
