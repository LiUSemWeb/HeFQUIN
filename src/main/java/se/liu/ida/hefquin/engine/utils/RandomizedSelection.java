package se.liu.ida.hefquin.engine.utils;

import java.util.Set;

public class RandomizedSelection<T extends RandomizedSelection.WeightedObject>
{
	public static interface WeightedObject
	{
		int getWeight();
	}

	/**
	 * Picks one of the elements from the given set by
	 * random and returns that element, where the weight
	 * of each element determines the probability for the
	 * element to be picked. That is, if an element E in the
	 * set has a weight of X and the sum of all weights of
	 * all elements in the set is Y, then the probability of
	 * element E is X/Y.
	 */
	public T pickOne( final Set<T> element ) {
		// TODO: this method needs to be implemented
		return null;
	}

}
