package se.liu.ida.hefquin.queryplan.executable.impl.pullbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import se.liu.ida.hefquin.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.queryproc.ExecutionContext;

public class ResultElementIterWithUnaryExecOpTest
{
	@Test
	public void getOpTest() {
		final UnaryExecutableOp1ForTest op = new UnaryExecutableOp1ForTest();
		final ResultElementIterWithUnaryExecOp<String,String> it = new ResultElementIterWithUnaryExecOp<String,String>(
				op,
				TestUtils.createResultBlockIteratorForTests(new String[]{"1","2","3"}, 2),
				TestUtils.createExecContextForTests() );

		assertEquals( op, it.getOp() );
	}

	@Test
	public void nextWithoutHasNext() {
		final ResultElementIterator<String> it = createIterator1ForTests( new String[]{"1","2","3"}, 2 );

		assertEquals( "1ok", it.next() );
		assertEquals( "2ok", it.next() );
		assertEquals( "3ok", it.next() );
		assertEquals( "added", it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void nextWithHasNext() {
		final ResultElementIterator<String> it = createIterator1ForTests( new String[]{"1","2","3"}, 2 );

		assertTrue( it.hasNext() );
		assertEquals( "1ok", it.next() );
		assertTrue( it.hasNext() );
		assertEquals( "2ok", it.next() );
		assertTrue( it.hasNext() );
		assertEquals( "3ok", it.next() );
		assertTrue( it.hasNext() );
		assertEquals( "added", it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noElementFromInput() {
		final ResultElementIterator<String> it = createIterator1ForTests( new String[]{}, 2 );

		assertTrue( it.hasNext() );
		assertEquals( "added", it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noElementAtAll() {
		final ResultElementIterator<String> it = createIterator2ForTests( new String[]{}, 2 );

		assertFalse( it.hasNext() );
	}



	protected static ResultElementIterator<String> createIterator1ForTests( final String[] elements, final int blockSize ) {
		final UnaryExecutableOp<String,String> op = new UnaryExecutableOp1ForTest();
		return new ResultElementIterWithUnaryExecOp<String,String>(
				op,
				TestUtils.createResultBlockIteratorForTests(elements, blockSize),
				TestUtils.createExecContextForTests() );
	}

	protected static ResultElementIterator<String> createIterator2ForTests( final String[] elements, final int blockSize ) {
		final UnaryExecutableOp<String,String> op = new UnaryExecutableOp2ForTest();
		return new ResultElementIterWithUnaryExecOp<String,String>(
				op,
				TestUtils.createResultBlockIteratorForTests(elements, blockSize),
				TestUtils.createExecContextForTests() );
	}

	protected static class UnaryExecutableOp1ForTest extends UnaryExecutableOp2ForTest
	{
		@Override
		public void concludeExecution( final IntermediateResultElementSink<String> sink,
		                               final ExecutionContext execCxt )
		{
			sink.send("added");
		}
	}

	protected static class UnaryExecutableOp2ForTest implements UnaryExecutableOp<String,String>
	{
		@Override
		public void process( final IntermediateResultBlock<String> input,
		                     final IntermediateResultElementSink<String> sink,
		                     final ExecutionContext execCxt )
		{
			final Iterator<String> it = input.iterator();
			while ( it.hasNext() ) {
				sink.send( it.next() + "ok" );
			}
		}

		@Override
		public void concludeExecution( final IntermediateResultElementSink<String> sink,
		                               final ExecutionContext execCxt )
		{
		}
	}

}
