package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.sparql.expr.ExprList;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class FilteringIterableForSolMaps_ExprList implements Iterable<SolutionMapping>
{
	protected final Iterable<SolutionMapping> input;
	protected final ExprList filterExpressions;

	public FilteringIterableForSolMaps_ExprList( final Iterable<SolutionMapping> input,
	                                             final ExprList filterExpressions ) {
		assert input != null;
		assert filterExpressions != null;

		this.input = input;
		this.filterExpressions = filterExpressions;
	}

	@Override
	public Iterator<SolutionMapping> iterator() {
		return new FilteringIteratorForSolMaps_ExprList(input, filterExpressions);
	}

}
