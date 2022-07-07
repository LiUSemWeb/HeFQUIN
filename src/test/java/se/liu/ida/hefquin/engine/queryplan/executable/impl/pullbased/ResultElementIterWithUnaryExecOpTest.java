package se.liu.ida.hefquin.engine.queryplan.executable.impl.pullbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecutableOperatorStats;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.ops.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;

public class ResultElementIterWithUnaryExecOpTest
{
	@Test
	public void getOpTest() {
		final UnaryExecutableOp1ForTest op = new UnaryExecutableOp1ForTest();
		final ResultElementIterWithUnaryExecOp it = new ResultElementIterWithUnaryExecOp(
				op,
				TestUtils.createResultBlockIteratorForTests(2),
				TestUtils.createExecContextForTests() );

		assertEquals( op, it.getOp() );
	}

	@Test
	public void nextWithoutHasNext() {
		final SolutionMapping sm1 = TestUtils.createSolutionMappingForTests("1");
		final SolutionMapping sm2 = TestUtils.createSolutionMappingForTests("2");
		final SolutionMapping sm3 = TestUtils.createSolutionMappingForTests("3");
		final ResultElementIterator it = createIterator1ForTests( 2, sm1, sm2, sm3 );

		assertEquals( "1ok", it.next().toString() );
		assertEquals( "2ok", it.next().toString() );
		assertEquals( "3ok", it.next().toString() );
		assertEquals( "added", it.next().toString() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void nextWithHasNext() {
		final SolutionMapping sm1 = TestUtils.createSolutionMappingForTests("1");
		final SolutionMapping sm2 = TestUtils.createSolutionMappingForTests("2");
		final SolutionMapping sm3 = TestUtils.createSolutionMappingForTests("3");
		final ResultElementIterator it = createIterator1ForTests( 2, sm1, sm2, sm3 );

		assertTrue( it.hasNext() );
		assertEquals( "1ok", it.next().toString() );
		assertTrue( it.hasNext() );
		assertEquals( "2ok", it.next().toString() );
		assertTrue( it.hasNext() );
		assertEquals( "3ok", it.next().toString() );
		assertTrue( it.hasNext() );
		assertEquals( "added", it.next().toString() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noElementFromInput() {
		final ResultElementIterator it = createIterator1ForTests( 2 );

		assertTrue( it.hasNext() );
		assertEquals( "added", it.next().toString() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noElementAtAll() {
		final ResultElementIterator it = createIterator2ForTests( 2 );

		assertFalse( it.hasNext() );
	}



	protected static ResultElementIterator createIterator1ForTests( final int blockSize, final SolutionMapping... elements ) {
		final UnaryExecutableOp op = new UnaryExecutableOp1ForTest();
		return new ResultElementIterWithUnaryExecOp(
				op,
				TestUtils.createResultBlockIteratorForTests(blockSize, elements),
				TestUtils.createExecContextForTests() );
	}

	protected static ResultElementIterator createIterator2ForTests( final int blockSize, final SolutionMapping... elements ) {
		final UnaryExecutableOp op = new UnaryExecutableOp2ForTest();
		return new ResultElementIterWithUnaryExecOp(
				op,
				TestUtils.createResultBlockIteratorForTests(blockSize, elements),
				TestUtils.createExecContextForTests() );
	}

	protected static class UnaryExecutableOp1ForTest extends UnaryExecutableOp2ForTest
	{
		@Override
		public void concludeExecution( final IntermediateResultElementSink sink,
		                               final ExecutionContext execCxt )
		{
			sink.send( TestUtils.createSolutionMappingForTests("added") );
		}
	}

	protected static class UnaryExecutableOp2ForTest implements UnaryExecutableOp
	{
		@Override
		public void process( final IntermediateResultBlock input,
		                     final IntermediateResultElementSink sink,
		                     final ExecutionContext execCxt )
		{
			final Iterator<SolutionMapping> it = input.getSolutionMappings().iterator();
			while ( it.hasNext() ) {
				final String token = it.next().toString() + "ok";
				sink.send( TestUtils.createSolutionMappingForTests(token) );
			}
		}

		@Override
		public void concludeExecution( final IntermediateResultElementSink sink,
		                               final ExecutionContext execCxt )
		{
		}

		@Override
		public int preferredInputBlockSize() { return 1; }

		@Override
		public void resetStats()
		{
		}

		@Override
		public ExecutableOperatorStats getStats() {
			return null;
		}
	}

}
