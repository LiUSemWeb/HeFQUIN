package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import se.liu.ida.hefquin.data.SolutionMapping;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.impl.GenericIntermediateResultBlockBuilderImpl;

public class ResultBlockIterOverResultElementIterTest
{
	@Test
	public void nextWithoutHasNext() {
		final SolutionMapping sm1 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm2 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm3 = TestUtils.createSolutionMappingForTests();
		final ResultBlockIterator blockIter = createIteratorForTests( 2, sm1, sm2, sm3 );

		final IntermediateResultBlock b1 = blockIter.next();
		assertEquals( 2, b1.size() );

		final Iterator<SolutionMapping> b1Iter = b1.iterator();
		assertEquals( sm1, b1Iter.next() );
		assertEquals( sm2, b1Iter.next() );

		final IntermediateResultBlock b2 = blockIter.next();
		assertEquals( 1, b2.size() );

		final Iterator<SolutionMapping> b2Iter = b2.iterator();
		assertEquals( sm3, b2Iter.next() );

		assertFalse( blockIter.hasNext() );
	}

	@Test
	public void nextWithHasNext() {
		final SolutionMapping sm1 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm2 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm3 = TestUtils.createSolutionMappingForTests();
		final ResultBlockIterator blockIter = createIteratorForTests( 2, sm1, sm2, sm3 );

		assertTrue( blockIter.hasNext() );
		final IntermediateResultBlock b1 = blockIter.next();
		assertEquals( 2, b1.size() );

		final Iterator<SolutionMapping> b1Iter = b1.iterator();
		assertEquals( sm1, b1Iter.next() );
		assertEquals( sm2, b1Iter.next() );

		assertTrue( blockIter.hasNext() );
		final IntermediateResultBlock b2 = blockIter.next();
		assertEquals( 1, b2.size() );

		final Iterator<SolutionMapping> b2Iter = b2.iterator();
		assertEquals( sm3, b2Iter.next() );

		assertFalse( blockIter.hasNext() );
	}

	@Test
	public void noElement() {
		final ResultBlockIterator blockIter = createIteratorForTests( 2 );

		assertFalse( blockIter.hasNext() );
	}


	protected static ResultBlockIterator createIteratorForTests( final int blockSize, final SolutionMapping... elements ) {
		return new ResultBlockIterOverResultElementIter(
				TestUtils.createResultElementIteratorForTests(elements),
				new GenericIntermediateResultBlockBuilderImpl(),
				blockSize );
	}

}
