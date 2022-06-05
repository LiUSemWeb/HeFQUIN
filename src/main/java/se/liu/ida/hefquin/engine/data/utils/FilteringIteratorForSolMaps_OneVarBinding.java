package se.liu.ida.hefquin.engine.data.utils;

import java.util.Iterator;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

/**
 * This is an iterator of solution mappings that consumes another iterator
 * and passes on only the solution mappings that have a given value for a
 * given variable.
 */
public class FilteringIteratorForSolMaps_OneVarBinding extends FilteringIteratorForSolMapsBase
{
	protected final Var var;
	protected final Node value;

	public FilteringIteratorForSolMaps_OneVarBinding( final Iterator<SolutionMapping> input, final Var var, final Node value ) {
		super(input);
		this.var   = var;
		this.value = value;
	}

	public FilteringIteratorForSolMaps_OneVarBinding( final Iterable<SolutionMapping> input, final Var var, final Node value ) {
		this( input.iterator(), var, value );
	}

	@Override
	protected SolutionMapping applyFilter( final SolutionMapping sm ) {
		final Node inputValue = sm.asJenaBinding().get(var);
		if ( inputValue == null || inputValue.equals(value) ) {
			return sm;
		}

		return null;
	}

}
