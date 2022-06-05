package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.FilteringIteratorForSolMaps_OneVarBinding;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableWithOneVarFilter;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableWithThreeVarsFilter;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingsIterableWithTwoVarsFilter;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;

public abstract class SolutionMappingsIndexBase implements SolutionMappingsIndex
{
	@Override
	public boolean containsAll( final Collection<?> c ) {
		for ( Object o : c ) {
			if ( ! contains(o) ) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean addAll( final Collection<? extends SolutionMapping> c ) {
		boolean changed = false;
		for ( SolutionMapping sm : c ) {
			changed = add(sm) || changed;
		}
		return changed;
	}

	@Override
	public boolean removeAll( final Collection<?> c ) {
		boolean changed = false;
		for ( Object o : c ) {
			changed = remove(o) || changed;
		}
		return changed;
	}

	@Override
	public boolean retainAll( final Collection<?> c ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Object[] toArray() {
		throw new UnsupportedOperationException();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		throw new UnsupportedOperationException();
	}

	/**
	 * Wraps a {@link SolutionMappingsIterableWithOneVarFilter}
	 * around the output of {@link #getAllSolutionMappings()}.
	 */
	protected Iterable<SolutionMapping> findSolutionMappingsLastResort(
			final Var var, final Node value )
	{
		final Iterable<SolutionMapping> it = getAllSolutionMappings();
		return FilteringIteratorForSolMaps_OneVarBinding.createAsIterable(it, var, value);
	}

	/**
	 * Wraps a {@link SolutionMappingsIterableWithTwoVarsFilter}
	 * around the output of {@link #getAllSolutionMappings()}.
	 */
	protected Iterable<SolutionMapping> findSolutionMappingsLastResort(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
	{
		final Iterable<SolutionMapping> it = getAllSolutionMappings();
		return new SolutionMappingsIterableWithTwoVarsFilter(it, var1, value1, var2, value2);
	}

	/**
	 * Wraps a {@link SolutionMappingsIterableWithThreeVarsFilter}
	 * around the output of {@link #getAllSolutionMappings()}.
	 */
	protected Iterable<SolutionMapping> findSolutionMappingsLastResort(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
	{
		final Iterable<SolutionMapping> it = getAllSolutionMappings();
		return new SolutionMappingsIterableWithThreeVarsFilter(it, var1, value1, var2, value2, var3, value3);
	}

}
