package se.liu.ida.hefquin.engine.queryplan.executable.impl.iterbased;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.NoSuchElementException;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;

public class SlicingResultElementIteratorTest
{
	@Test
	public void noOffsetNoLimit() {
		final Var v = Var.alloc("v");
		final ResultElementIterator input = new MyResultElementIteratorForTests(3, v);

		final ResultElementIterator it = new SlicingResultElementIterator(input, 0, -1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertTrue( it.hasNext() );
		assertEquals( 2, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertTrue( it.hasNext() );
		assertEquals( 3, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void noOffsetButLimit() {
		final Var v = Var.alloc("v");
		final ResultElementIterator input = new MyResultElementIteratorForTests(3, v);

		final ResultElementIterator it = new SlicingResultElementIterator(input, 0, 2);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertTrue( it.hasNext() );
		assertEquals( 2, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void offsetNoLimit() {
		final Var v = Var.alloc("v");
		final ResultElementIterator input = new MyResultElementIteratorForTests(3, v);

		final ResultElementIterator it = new SlicingResultElementIterator(input, 1, -1);

		assertTrue( it.hasNext() );
		assertEquals( 2, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertTrue( it.hasNext() );
		assertEquals( 3, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void offsetAndLimit() {
		final Var v = Var.alloc("v");
		final ResultElementIterator input = new MyResultElementIteratorForTests(3, v);

		final ResultElementIterator it = new SlicingResultElementIterator(input, 1, 1);

		assertTrue( it.hasNext() );
		assertEquals( 2, it.next().asJenaBinding().get(v).getLiteralValue() );

		assertFalse( it.hasNext() );
	}


	// --- helper functionality ---

	protected class MyResultElementIteratorForTests implements ResultElementIterator {
		public final Var var;
		protected final int resultSize;
		protected int outputCounter = 0;

		public MyResultElementIteratorForTests( final int resultSize,
		                                        final Var var ) {
			this.resultSize = resultSize;
			this.var = var;
		}

		@Override
		public boolean hasNext() throws ResultElementIterException {
			return outputCounter < resultSize;
		}

		@Override
		public SolutionMapping next() throws ResultElementIterException {
			if ( ! hasNext() )
				throw new NoSuchElementException();

			outputCounter++;

			final Node l = NodeFactory.createLiteralByValue(outputCounter);
			return SolutionMappingUtils.createSolutionMapping(var, l);
		}
	}
}
