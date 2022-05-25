package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.Iterator;

import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_ExprList;

public class ExecOpFilter extends UnaryExecutableOpBaseWithIterator
{
	protected final ExprList filterExpressions;

	public ExecOpFilter( final ExprList filterExpressions ) {
		assert filterExpressions != null;
		assert ! filterExpressions.isEmpty();

		this.filterExpressions = filterExpressions;
	}

	public ExecOpFilter( final Expr filterExpression ) {
		assert filterExpression != null;

		this.filterExpressions = new ExprList(filterExpression);
	}

	@Override
	protected Iterator<SolutionMapping> createInputToOutputIterator( final Iterable<SolutionMapping> input ) {
		return new FilteringIteratorForSolMaps_ExprList(input, filterExpressions);
	}

}
