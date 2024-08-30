package se.liu.ida.hefquin.base.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

public class ConcatenatingIterableTest {

	@Test
	public void testIteratorConcatenatesElementsCorrectly() {
		final List<Integer> list1 = Arrays.asList(1, 2, 3);
		final List<Integer> list2 = Arrays.asList(4, 5, 6);
		final Iterable<Integer> iterable = new ConcatenatingIterable<>(list1, list2);

		final Iterator<Integer> iterator = iterable.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(1, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(3, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(4, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(5, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(6, iterator.next().intValue());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorWithEmptyFirstIterable() {
		List<Integer> emptyList = Arrays.asList();
		List<Integer> list2 = Arrays.asList(4, 5, 6);
		Iterable<Integer> iterable = new ConcatenatingIterable<>(emptyList, list2);

		Iterator<Integer> iterator = iterable.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(4, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(5, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(6, iterator.next().intValue());
		assertFalse(iterator.hasNext());
	}

	@Test
	public void testIteratorWithEmptySecondIterable() {
		List<Integer> list1 = Arrays.asList(1, 2, 3);
		List<Integer> emptyList = Arrays.asList();
		Iterable<Integer> iterable = new ConcatenatingIterable<>(list1, emptyList);

		Iterator<Integer> iterator = iterable.iterator();
		assertTrue(iterator.hasNext());
		assertEquals(1, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(2, iterator.next().intValue());
		assertTrue(iterator.hasNext());
		assertEquals(3, iterator.next().intValue());
		assertFalse(iterator.hasNext());
	}
}
