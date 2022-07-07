package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.NullaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ResultElementIterWithNullaryExecOpTest
{
	@Test
	public void getOpTest() {
		final NullaryExecutableOpForTest op = new NullaryExecutableOpForTest();
		final ResultElementIterWithNullaryExecOp it = new ResultElementIterWithNullaryExecOp( op, TestUtils.createExecContextForTests() );

		assertEquals( op, it.getOp() );
	}

	@Test
	public void nextWithoutHasNext() {
		final SolutionMapping sm1 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm2 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm3 = TestUtils.createSolutionMappingForTests();
		final ResultElementIterator it = createIteratorForTests( sm1, sm2, sm3 );

		assertEquals( sm1, it.next() );
		assertEquals( sm2, it.next() );
		assertEquals( sm3, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void nextWithHasNext() {
		final SolutionMapping sm1 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm2 = TestUtils.createSolutionMappingForTests();
		final SolutionMapping sm3 = TestUtils.createSolutionMappingForTests();
		final ResultElementIterator it = createIteratorForTests( sm1, sm2, sm3 );

		assertTrue( it.hasNext() );
		assertEquals( sm1, it.next() );
		assertTrue( it.hasNext() );
		assertEquals( sm2, it.next() );
		assertTrue( it.hasNext() );
		assertEquals( sm3, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noElement() {
		final ResultElementIterator it = createIteratorForTests();

		assertFalse( it.hasNext() );
	}


	protected static ResultElementIterator createIteratorForTests( SolutionMapping... elements ) {
		return new ResultElementIterWithNullaryExecOp(
						new NullaryExecutableOpForTest(elements),
						TestUtils.createExecContextForTests() );
	}

	protected static class NullaryExecutableOpForTest implements NullaryExecutableOp
	{
		final List<SolutionMapping> list;

		public NullaryExecutableOpForTest() {
			list = null;
		}

		public NullaryExecutableOpForTest( final SolutionMapping[] elements ) {
			list = Arrays.asList(elements);
		}

		@Override
		public void execute( final IntermediateResultElementSink sink,
		                     final ExecutionContext execCxt )
		{
			if ( list != null ) {
				for ( final SolutionMapping sm : list ) {
					sink.send(sm);
				}
			}
		}

		@Override
		public void resetStats() {
		}

		@Override
		public ExecutableOperatorStats getStats() {
			return null;
		}
	}

}
