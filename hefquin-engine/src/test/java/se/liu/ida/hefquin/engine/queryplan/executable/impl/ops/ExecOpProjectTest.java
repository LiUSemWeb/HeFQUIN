package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;

public class ExecOpProjectTest
{
	@Test
	public void projectKeepCorrectVariable() throws ExecOpExecutionException {
		// Tests the case in which the correct variable is kept
		// and that the correct variable is dropped.

		final Node URI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node URI2 = NodeFactory.createURI("http://example.org/uri2");
		final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");

		final Set<Var> variables = Set.of(var1);

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, URI1);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, URI1, var2, URI2);

		final ExecOpProject op = new ExecOpProject(variables, false, false, null);
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(sm1, sink, null);
		op.process(sm2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );

		final Set<Var> outVars1 = it.next().asJenaBinding().varsMentioned();
		assertTrue( outVars1.contains(var1) );
		assertEquals( 1, outVars1.size() );

		assertTrue( it.hasNext() );

		final Set<Var> outVars2 = it.next().asJenaBinding().varsMentioned();
		assertTrue( outVars2.contains(var1) );
		assertEquals( 1, outVars2.size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void projectMultipleProjectedVariables() throws ExecOpExecutionException {
		// Tests the case in which there are multiple projected variables.

		final Node URI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node URI2 = NodeFactory.createURI("http://example.org/uri2");
		final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");
		final Var var3 = Var.alloc("var3");

		final Set<Var> variables = Set.of(var1,var2);

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, URI1, var3, URI2);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, URI1, var2, URI2, var3, URI2);

		final ExecOpProject op = new ExecOpProject(variables, false, false, null);
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(sm1, sink, null);
		op.process(sm2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		boolean foundVar2 = false;

		assertTrue( it.hasNext() );

		final Set<Var> outVars1 = it.next().asJenaBinding().varsMentioned();
		assertTrue( outVars1.contains(var1) );
		assertEquals( 1, outVars1.size() );
		if ( outVars1.contains(var2) ) foundVar2=true;

		assertTrue( it.hasNext() );

		final Set<Var> outVars2 = it.next().asJenaBinding().varsMentioned();
		assertTrue( outVars2.contains(var1) );
		assertEquals( 2, outVars2.size() );
		if ( outVars2.contains(var2) ) foundVar2=true;

		assertTrue( foundVar2 );

		assertFalse( it.hasNext() );
	}

	@Test
	public void projectAllVariablesInProjectionSet() throws ExecOpExecutionException {
		// Tests the case in which all input variables are included
		// in the projected variables set, to ensure that nothing
		// gets lost unexpectedly.

		final Node URI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node URI2 = NodeFactory.createURI("http://example.org/uri2");
		final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");

		final Set<Var> variables = Set.of(var1, var2);

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, URI1, var2, URI2);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, URI1, var2, URI2);

		final ExecOpProject op = new ExecOpProject(variables, false, false, null);
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(sm1, sink, null);
		op.process(sm2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );

		final Set<Var> outVars1 = it.next().asJenaBinding().varsMentioned();
		assertTrue( outVars1.contains(var1) );
		assertTrue( outVars1.contains(var2) );
		assertEquals( 2, outVars1.size() );

		assertTrue( it.hasNext() );

		final Set<Var> outVars2 = it.next().asJenaBinding().varsMentioned();
		assertTrue( outVars2.contains(var1) );
		assertTrue( outVars2.contains(var2) );
		assertEquals( 2, outVars2.size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void projectDisjointVariables() throws ExecOpExecutionException {
		// Tests the case where the input variables are disjoint
		// from the projected variables. The expected output
		// is empty solution mappings.

		final Node URI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node URI2 = NodeFactory.createURI("http://example.org/uri2");
		final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");
		final Var var3 = Var.alloc("var3");
		final Var var4 = Var.alloc("var4");


		final Set<Var> variables = Set.of(var3, var4);

		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(var1, URI1);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(var1, URI1, var2, URI2);

		final ExecOpProject op = new ExecOpProject(variables, false, false, null);
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		op.process(sm1, sink, null);
		op.process(sm2, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );

		assertEquals( true, it.next().asJenaBinding().isEmpty() );

		assertTrue( it.hasNext() );

		assertEquals( true, it.next().asJenaBinding().isEmpty() );

		assertFalse( it.hasNext() );
	}
}
