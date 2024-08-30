package se.liu.ida.hefquin.base.datastructures.impl;

import java.util.Collection;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.datastructures.SolutionMappingsIndex;
import se.liu.ida.hefquin.base.utils.Stats;

/**
 * Base class for implementations of {@link SolutionMappingsIndex}
 * that wrap another {@link SolutionMappingsIndex}. This base class
 * simply forwards all method calls to the wrapped index. Subclasses
 * may override this behavior for selected methods.
 */
public class WrappingSolutionMappingsIndex implements SolutionMappingsIndex
{
	protected final SolutionMappingsIndex wrappedIndex;

	public WrappingSolutionMappingsIndex( final SolutionMappingsIndex wrappedIndex ) {
		assert wrappedIndex != null;
		this.wrappedIndex = wrappedIndex;
	}

	@Override
	public Stats getStats() {
		return wrappedIndex.getStats();
	}

	@Override
	public void resetStats() {
		wrappedIndex.resetStats();
	}

	@Override
	public int size() {
		return wrappedIndex.size();
	}

	@Override
	public boolean isEmpty() {
		return wrappedIndex.isEmpty();
	}

	@Override
	public boolean contains( final Object o ) {
		return wrappedIndex.contains(o);
	}

	@Override
	public Object[] toArray() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T[] toArray( final T[] a ) {
		return wrappedIndex.toArray(a);
	}

	@Override
	public boolean add( final SolutionMapping e ) {
		return wrappedIndex.add(e);
	}

	@Override
	public boolean remove( final Object o ) {
		return wrappedIndex.remove(o);
	}

	@Override
	public boolean containsAll( final Collection<?> c ) {
		return wrappedIndex.containsAll(c);
	}

	@Override
	public boolean addAll( final Collection<? extends SolutionMapping> c ) {
		return wrappedIndex.addAll(c);
	}

	@Override
	public boolean removeAll( final Collection<?> c ) {
		return wrappedIndex.removeAll(c);
	}

	@Override
	public boolean retainAll( final Collection<?> c ) {
		return wrappedIndex.retainAll(c);
	}

	@Override
	public void clear() {
		wrappedIndex.clear();
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
			throws UnsupportedOperationException {
		return wrappedIndex.getJoinPartners(sm);
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var, final Node value )
					throws UnsupportedOperationException
	{
		return wrappedIndex.findSolutionMappings(var, value);
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
					throws UnsupportedOperationException
	{
		return wrappedIndex.findSolutionMappings(var1, value1, var2, value2);
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
					throws UnsupportedOperationException
	{
		return wrappedIndex.findSolutionMappings(var1, value1, var2, value2, var3, value3);
	}

	@Override
	public Iterable<SolutionMapping> getAllSolutionMappings() {
		return wrappedIndex.getAllSolutionMappings();
	}

}
