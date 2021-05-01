package se.liu.ida.hefquin.datastructures.impl;

import java.util.Collection;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.datastructures.SolutionMappingsIndex;

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

}
