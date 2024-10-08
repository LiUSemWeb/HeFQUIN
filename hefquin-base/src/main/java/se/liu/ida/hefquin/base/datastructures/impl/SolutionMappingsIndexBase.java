package se.liu.ida.hefquin.base.datastructures.impl;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.FilteringIteratorForSolMaps_OneVarBinding;
import se.liu.ida.hefquin.base.data.utils.FilteringIteratorForSolMaps_ThreeVarsBindings;
import se.liu.ida.hefquin.base.data.utils.FilteringIteratorForSolMaps_TwoVarsBindings;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.base.utils.Stats;

public abstract class SolutionMappingsIndexBase implements SolutionMappingsIndex
{
	@Override
	public Stats getStats() {
		// TODO
		return null;
	}

	@Override
	public void resetStats() {
		// TODO
	}

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
		return FilteringIteratorForSolMaps_TwoVarsBindings.createAsIterable(it, var1, value1, var2, value2);
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
		return FilteringIteratorForSolMaps_ThreeVarsBindings.createAsIterable(it, var1, value1, var2, value2, var3, value3);
	}

}
