package se.liu.ida.hefquin.engine.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class IteratorFactoryBasedIterableTest
{
	@Test
	public void testIteratorReturnsElementsFromFactory() {
		final List<Integer> expectedElements = Arrays.asList(1, 2, 3);
		final IteratorFactory<Integer> factory = () -> expectedElements.iterator();

		final Iterable<Integer> iterable = new IteratorFactoryBasedIterable<>(factory);
		final Iterator<Integer> iterator = iterable.iterator();

		assertTrue(iterator.hasNext());
		assertEquals(1, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(3, iterator.next().intValue());
		assertFalse(iterator.hasNext());
	}
}
