package se.liu.ida.hefquin.base.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class WrappingIterableTest {

    @Test
    public void testIterator_emptyList() {
        // Arrange
        final Iterable<String> input = Collections.emptyList();
        final WrappingIteratorFactory<String> itFactory = (inputIterator) -> inputIterator;

        final Iterable<String> wrappingIterable = new WrappingIterable<>(input, itFactory);

        // Act
        final Iterator<String> iterator = wrappingIterable.iterator();

        // Assert
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIterator_singleElement() {
        // Arrange
        final List<Integer> input = Collections.singletonList(1);
        final WrappingIteratorFactory<Integer> itFactory = (inputIterator) -> new Iterator<Integer>() {
            @Override
            public boolean hasNext() {
                return inputIterator.hasNext();
            }

            @Override
            public Integer next() {
                return inputIterator.next() + 1;
            }
        };

        final Iterable<Integer> wrappingIterable = new WrappingIterable<>(input, itFactory);

        // Act
        final Iterator<Integer> iterator = wrappingIterable.iterator();

        // Assert
        assertTrue(iterator.hasNext());
        assertEquals(2, (int) iterator.next());
        assertFalse(iterator.hasNext());
    }

    @Test
    public void testIterator_multipleElements() {
        // Arrange
        final List<String> input = new ArrayList<>();
        input.add("apple");
        input.add("banana");
        input.add("cherry");
        final WrappingIteratorFactory<String> itFactory = (inputIterator) -> new Iterator<String>() {
            private final String prefix = "modified-";
            private final Iterator<String> originalIterator = inputIterator;

            @Override
            public boolean hasNext() {
                return originalIterator.hasNext();
            }

            @Override
            public String next() {
                return prefix + originalIterator.next();
            }
        };

        final Iterable<String> wrappingIterable = new WrappingIterable<>(input, itFactory);

        // Act
        final Iterator<String> iterator = wrappingIterable.iterator();

        // Assert
        assertTrue(iterator.hasNext());
        assertEquals("modified-apple", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("modified-banana", iterator.next());
        assertTrue(iterator.hasNext());
        assertEquals("modified-cherry", iterator.next());
        assertFalse(iterator.hasNext());
    }

}
