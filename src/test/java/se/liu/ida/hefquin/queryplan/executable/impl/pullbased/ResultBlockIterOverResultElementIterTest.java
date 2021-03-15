package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.junit.Test;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;

public class ResultBlockIterOverResultElementIterTest
{
	@Test
	public void nextWithoutHasNext() { 
		final ResultBlockIterator<String> blockIter = createIteratorForTests( new String[] {"1", "2", "3"}, 2 );

		final IntermediateResultBlock<String> b1 = blockIter.next();
		assertEquals( 2, b1.size() );

		final Iterator<String> b1Iter = b1.iterator();
		assertEquals( "1", b1Iter.next() );
		assertEquals( "2", b1Iter.next() );

		final IntermediateResultBlock<String> b2 = blockIter.next();
		assertEquals( 1, b2.size() );

		final Iterator<String> b2Iter = b2.iterator();
		assertEquals( "3", b2Iter.next() );

		assertFalse( blockIter.hasNext() );
	}

	@Test
	public void nextWithHasNext() { 
		final ResultBlockIterator<String> blockIter = createIteratorForTests( new String[] {"1", "2", "3"}, 2 );

		assertTrue( blockIter.hasNext() );
		final IntermediateResultBlock<String> b1 = blockIter.next();
		assertEquals( 2, b1.size() );

		final Iterator<String> b1Iter = b1.iterator();
		assertEquals( "1", b1Iter.next() );
		assertEquals( "2", b1Iter.next() );

		assertTrue( blockIter.hasNext() );
		final IntermediateResultBlock<String> b2 = blockIter.next();
		assertEquals( 1, b2.size() );

		final Iterator<String> b2Iter = b2.iterator();
		assertEquals( "3", b2Iter.next() );

		assertFalse( blockIter.hasNext() );
	}

	@Test
	public void noElement() {
		final ResultBlockIterator<String> blockIter = createIteratorForTests( new String[] {}, 2 );

		assertFalse( blockIter.hasNext() );
	}


	protected static ResultBlockIterator<String> createIteratorForTests( final String[] elements, final int blockSize ) {
		return new ResultBlockIterOverResultElementIter<String>(
				TestUtils.createResultElementIteratorForTests(elements),
				new GenericIntermediateResultBlockBuilderImpl<String>(),
				blockSize );
	}

}
