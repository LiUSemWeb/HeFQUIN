package se.liu.ida.hefquin.engine.utils;

import java.util.Iterator;
import java.util.NavigableMap;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;

public class RandomizedSelection<T extends RandomizedSelection.WeightedObject>
{
	public static interface WeightedObject
	{
		double getWeight();
	}

	/**
	 * Picks one of the elements from the given set by random and returns
	 * that element, where the weights of the elements determine their
	 * respective probability to be picked. That is, if an element E in
	 * the set has a weight of X and the sum of all weights of all elements
	 * in the set is Y, then the probability of element E is X/Y.
	 *
	 * Note that this method needs to populate an internal data structure.
	 * Therefore, if you want to pick multiple times (independent from one
	 * another), it is better to do so via an iterator that can be obtained
	 * with {@link #getRandomlyPickingIterator(Set)}.
	 */
	public T pickOne( final Set<T> elements ) {
		return getRandomlyPickingIterator(elements).next();
	}

	/**
	 * Returns an infinite iterator that can be used to continuously
	 * pick elements from the given set at random, where the weights
	 * of the elements determine their respective probability to be
	 * picked. That is, if an element E in the set has a weight of X
	 * and the sum of all weights of all elements in the set is Y,
	 * then the probability of element E is X/Y. The probabilities
	 * are not affected by which elements have been picked before.
	 */
	public Iterator<T> getRandomlyPickingIterator( final Set<T> elements ) {
		return new MyIterator(elements);
	}


	// The following helper class is adapted from:
	// https://stackoverflow.com/questions/6409652/random-weighted-selection-in-java
	protected class MyIterator implements Iterator<T>
	{
		protected final NavigableMap<Double, T> map = new TreeMap<>();
		protected final Random random = new Random();
		protected final double total;

		public MyIterator( final Set<T> elements ) {
			double total = 0;
			for ( final T element : elements ) {
				if ( element.getWeight() > 0 ) {
					total += element.getWeight();
					map.put(total, element);
				}
			}
			this.total = total;
		}

		@Override
		public boolean hasNext() {
			return true; // there is always a next one
		}


		@Override
		public T next() {
			final double value = random.nextDouble() * total;
			return map.higherEntry(value).getValue();
		}
	}

}
