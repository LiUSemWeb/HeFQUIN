package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;

public class TestsForUnionAlgorithms extends ExecOpTestBase {

	protected void _testSimpleUnion() {
		final Var x = Var.alloc("x");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node x3 = NodeFactory.createURI("http://example.org/x3");
		
		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(x, x1) );
		
		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(x, x2) );
		input2.add( SolutionMappingUtils.createSolutionMapping(x, x3) );
		
		final Iterator<SolutionMapping> it = runTest(input1, input2);
		
		final Set<Binding> result = new HashSet<>();
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertFalse( it.hasNext() );
	
		boolean b1Found = false;
		boolean b2Found = false;
		boolean b3Found = false;
		
		for ( final Binding b : result ) {
			assertEquals( 1, b.size() );
			if (b.get(x).getURI().equals("http://example.org/x1"))
				b1Found = true;
			else if (b.get(x).getURI().equals("http://example.org/x2"))
				b2Found = true;
			else if ((b.get(x).getURI().equals("http://example.org/x3")))
				b3Found = true;
			else {
				fail( "Unexpected URI for ?x: " + b.get(x).getURI() );
			}
		}
		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);
	}
	
	protected void _testUnboundUnion() {
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		
		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node x3 = NodeFactory.createURI("http://example.org/x3");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node y3 = NodeFactory.createURI("http://example.org/y3");
		
		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(x, x1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(x, x3, y, y3) );
		
		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(y, y1) );
		input2.add( SolutionMappingUtils.createSolutionMapping(y, y2, x, x2) );
		
		final Iterator<SolutionMapping> it = runTest(input1, input2);
		
		final List<Binding> result = new ArrayList<>();
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertFalse( it.hasNext() );
	
		boolean b1Found = false;
		boolean b2Found = false;
		boolean b3Found = false;
		boolean b4Found = false;
		
		for ( final Binding b : result ) {
			if (b.get(x) != null) {
				if (b.get(x).getURI().equals("http://example.org/x1")) {
					assertEquals(1, b.size());
					b1Found = true;
				}
				else if (b.get(x).getURI().equals("http://example.org/x2")) {
					assertEquals(2, b.size());
					assertNotEquals(null, b.get(y));
					assertEquals("http://example.org/y2", b.get(y).getURI());
					b2Found = true;
				}
				else if (b.get(x).getURI().equals("http://example.org/x3")) {
					assertEquals(2, b.size());
					assertNotEquals(null, b.get(y));
					assertEquals("http://example.org/y3", b.get(y).getURI());
					b3Found = true;
				}
			}
			else if (b.get(y) != null && b.get(y).getURI().equals("http://example.org/y1")) {
				assertEquals(1, b.size());
				b4Found = true;
			}
		}
		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);
		assertTrue(b4Found);
	}
	
	protected void _testKeepUnionDuplicates() {
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		
		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node x3 = NodeFactory.createURI("http://example.org/x3");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node y3 = NodeFactory.createURI("http://example.org/y3");
		
		final GenericIntermediateResultBlockImpl input1 = new GenericIntermediateResultBlockImpl();
		input1.add( SolutionMappingUtils.createSolutionMapping(x, x1, y, y1) );
		input1.add( SolutionMappingUtils.createSolutionMapping(x, x2, y, y2) );
		
		final GenericIntermediateResultBlockImpl input2 = new GenericIntermediateResultBlockImpl();
		input2.add( SolutionMappingUtils.createSolutionMapping(x, x2, y, y2) );
		input2.add( SolutionMappingUtils.createSolutionMapping(x, x3, y, y3) );
		
		final Iterator<SolutionMapping> it = runTest(input1, input2);
		
		final List<Binding> result = new ArrayList<>();
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
	
		assertFalse( it.hasNext() );
	
		boolean b1Found = false;
		boolean b2Found = false;
		int b3Found = 0;
		
		for ( final Binding b : result ) {
			assertEquals(2, b.size());
			if (b.get(x).getURI().equals("http://example.org/x1")) {
				assertEquals("http://example.org/y1", b.get(y).getURI());
				b1Found = true;
			}
			else if (b.get(x).getURI().equals("http://example.org/x3")) {
				assertEquals("http://example.org/y3", b.get(y).getURI());
				b2Found = true;
			} 
			else if (b.get(x).getURI().equals("http://example.org/x2")) {
				assertEquals("http://example.org/y2", b.get(y).getURI());
				b3Found++;
			}
		}
		assertTrue(b1Found);
		assertTrue(b2Found);
		assertEquals(2, b3Found);
	}

	protected Iterator<SolutionMapping> runTest( final IntermediateResultBlock input1,
 			final IntermediateResultBlock input2 ) {
		
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		
		final BinaryExecutableOp op= new ExecOpBinaryUnion();
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);
		return sink.getMaterializedIntermediateResult().iterator();
	}
	
}
