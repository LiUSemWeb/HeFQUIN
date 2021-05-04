package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.MaterializingIntermediateResultElementSink;

public class TestsForUnionAlgorithms<MemberType extends FederationMember> extends ExecOpTestBase{

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

	private Iterator<SolutionMapping> runTest(GenericIntermediateResultBlockImpl input1,
			GenericIntermediateResultBlockImpl input2) {
		
		final MaterializingIntermediateResultElementSink sink = new MaterializingIntermediateResultElementSink();
		
		final BinaryExecutableOp op= new ExecOpBinaryUnion();
		op.processBlockFromChild1(input1, sink, null);
		op.processBlockFromChild2(input2, sink, null);
		return sink.getMaterializedIntermediateResult().iterator();
	}
	
}
