package se.liu.ida.hefquin.engine.datastructures.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.sparql.core.Var;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.datastructures.SolutionMappingsIndex;

/**
 * Wraps another {@link SolutionMappingsIndex} and, for every method that
 * returns an {@link Iterable} of solution mappings, this implementation
 * copies the {@link Iterable} returned by the wrapped index into a new
 * list and, then, returns that list.
 *
 * This class can be used for cases in which adding and probing into such
 * an index may happen concurrently. In such cases, there can be conflicts
 * when the index is updated by one thread while another thread is still
 * consuming an {@link Iterable} returned by one of the methods. Returning
 * a copy of the {@link Iterable} avoids such conflicts.
 */
public class SolutionMappingsIndexForMixedUsage extends WrappingSolutionMappingsIndex
{
	public SolutionMappingsIndexForMixedUsage( final SolutionMappingsIndex wrappedIndex ) {
		super(wrappedIndex);
	}

	@Override
	public <T> T[] toArray( final T[] a ) {
		throw new UnsupportedOperationException();
	}

	@Override
	public Iterable<SolutionMapping> getJoinPartners( final SolutionMapping sm )
			throws UnsupportedOperationException {
		return copy( wrappedIndex.getJoinPartners(sm) );
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var, final Node value )
					throws UnsupportedOperationException
	{
		return copy( wrappedIndex.findSolutionMappings(var, value) );
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2 )
					throws UnsupportedOperationException
	{
		return copy( wrappedIndex.findSolutionMappings(var1, value1, var2, value2) );
	}

	@Override
	public Iterable<SolutionMapping> findSolutionMappings(
			final Var var1, final Node value1,
			final Var var2, final Node value2,
			final Var var3, final Node value3 )
					throws UnsupportedOperationException
	{
		return copy( wrappedIndex.findSolutionMappings(var1, value1, var2, value2, var3, value3) );
	}

	@Override
	public Iterable<SolutionMapping> getAllSolutionMappings() {
		return copy( wrappedIndex.getAllSolutionMappings() );
	}


	public static Iterable<SolutionMapping> copy( final Iterable<SolutionMapping> i ) {
		final List<SolutionMapping> l = new ArrayList<>();
		for ( final SolutionMapping sm : i ) {
			l.add(sm);
		}
		return l;
	}

}
