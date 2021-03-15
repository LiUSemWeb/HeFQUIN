package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.NullaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithNullaryExecOpTest
{
	@Test
	public void getOpTest() {
		final NullaryExecutableOp1ForTest op = new NullaryExecutableOp1ForTest();
		final ResultElementIterWithNullaryExecOp<String> it = new ResultElementIterWithNullaryExecOp<String>( op, TestUtils.createExecContextForTests() );

		assertEquals( op, it.getOp() );
	}

	@Test
	public void nextWithoutHasNext() {
		final ResultElementIterator<String> it = createIterator1ForTests();

		assertEquals( "test str 1", it.next() );
		assertEquals( "test str 2", it.next() );
		assertEquals( "test str 3", it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void nextWithHasNext() {
		final ResultElementIterator<String> it = createIterator1ForTests();

		assertTrue( it.hasNext() );
		assertEquals( "test str 1", it.next() );
		assertTrue( it.hasNext() );
		assertEquals( "test str 2", it.next() );
		assertTrue( it.hasNext() );
		assertEquals( "test str 3", it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noElement() {
		final ResultElementIterator<String> it = createIterator2ForTests();

		assertFalse( it.hasNext() );
	}


	protected static ResultElementIterator<String> createIterator1ForTests() {
		return new ResultElementIterWithNullaryExecOp<String>(
						new NullaryExecutableOp1ForTest(),
						TestUtils.createExecContextForTests() );
	}

	protected static ResultElementIterator<String> createIterator2ForTests() {
		return new ResultElementIterWithNullaryExecOp<String>(
						new NullaryExecutableOp2ForTest(),
						TestUtils.createExecContextForTests() );
	}

	protected static class NullaryExecutableOp1ForTest implements NullaryExecutableOp<String>
	{
		@Override
		public void execute( final IntermediateResultElementSink<String> sink,
		                     final ExecutionContext execCxt )
		{
			sink.send("test str 1");
			sink.send("test str 2");
			sink.send("test str 3");
		}
	}

	protected static class NullaryExecutableOp2ForTest implements NullaryExecutableOp<String>
	{
		@Override
		public void execute( final IntermediateResultElementSink<String> sink,
		                     final ExecutionContext execCxt )
		{
		}
	}

}
