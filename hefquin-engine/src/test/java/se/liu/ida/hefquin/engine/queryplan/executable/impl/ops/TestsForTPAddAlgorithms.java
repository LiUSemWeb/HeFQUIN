package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.engine.binding.BindingFactory;
import org.apache.jena.sparql.graph.GraphFactory;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

/**
 * This is an abstract class with tests for any algorithm that is
 * meant to be used as an implementation for the tpAdd operator.
 */
public abstract class TestsForTPAddAlgorithms<MemberType extends FederationMember> extends ExecOpTestBase
{
	protected void _tpWithJoinOnObject( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");
		final Node z3 = NodeFactory.createURI("http://example.org/z3");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final TriplePattern tp = new TriplePatternImpl(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );
		dataForMember.add( Triple.create(y1,p,z2) );
		dataForMember.add( Triple.create(y2,p,z3) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() { return Set.of(var1, var2); }

			@Override
			public Set<Var> getPossibleVariables() { return Set.of(); }
		}, useOuterJoinSemantics);

		// checking
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
			assertEquals( 3, b.size() );

			if ( b.get(var1).getURI().equals("http://example.org/x1") ) {
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				if ( b.get(var3).getURI().equals("http://example.org/z1") ) {
					b1Found = true;
				}
				else if ( b.get(var3).getURI().equals("http://example.org/z2") ) {
					b2Found = true;
				}
				else {
					fail( "Unexpected URI for ?v3: " + b.get(var3).getURI() );
				}
			}
			else if ( b.get(var1).getURI().equals("http://example.org/x2") ) {
				assertEquals( "http://example.org/y2", b.get(var2).getURI() );
				assertEquals( "http://example.org/z3", b.get(var3).getURI() );
				b3Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);

	}

	protected void _tpWithJoinOnSubjectAndObject( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x1,
				var2, y1) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, x2,
				var2, y2) );

		final TriplePattern tp = new TriplePatternImpl(var1,var2,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(x1,y1,z1) );
		dataForMember.add( Triple.create(x2,y2,z2) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var1);
				set.add(var2);
				return set;
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return new HashSet<>();
			}
		}, useOuterJoinSemantics);

		// checking
		final Set<Binding> result = new HashSet<>();

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertFalse( it.hasNext() );

		boolean b1Found = false;
		boolean b2Found = false;
		for ( final Binding b : result ) {
			assertEquals( 3, b.size() );

			if ( b.get(var1).getURI().equals("http://example.org/x1") ) {
				assertEquals( "http://example.org/y1", b.get(var2).getURI() );
				assertEquals( "http://example.org/z1", b.get(var3).getURI() );
				b1Found = true;
			}
			else if ( b.get(var1).getURI().equals("http://example.org/x2") ) {
				assertEquals( "http://example.org/y2", b.get(var2).getURI() );
				assertEquals( "http://example.org/z2", b.get(var3).getURI() );
				b2Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
	}

	protected void _tpWithoutJoinVariable( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, x1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var1, x2) );

		final TriplePattern tp = new TriplePatternImpl(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );
		dataForMember.add( Triple.create(y2,p,z2) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var1);
				return set;
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return new HashSet<>();
			}
		}, useOuterJoinSemantics);

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 3, b1.size() );

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 3, b2.size() );

		assertTrue( it.hasNext() );
		final Binding b3 = it.next().asJenaBinding();
		assertEquals( 3, b3.size() );

		assertTrue( it.hasNext() );
		final Binding b4 = it.next().asJenaBinding();
		assertEquals( 3, b4.size() );

		assertFalse( it.hasNext() );
	}

	protected void _tpWithAndWithoutJoinVariable( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, x1) ); // join variable
		input.add( SolutionMappingUtils.createSolutionMapping(var2, x2) ); // no join variable

		final TriplePattern tp = new TriplePatternImpl(var1,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(x1,p,z1) );
		dataForMember.add( Triple.create(y2,p,z2) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() { return Set.of(); }

			@Override
			public Set<Var> getPossibleVariables() { return Set.of(var1, var2); }
		}, useOuterJoinSemantics);

		final Binding expected1 = BindingFactory.binding(var1, x1, var3, z1);
		final Binding expected2 = BindingFactory.binding(var1, x1, var3, z1, var2, x2);
		final Binding expected3 = BindingFactory.binding(var1, y2, var3, z2, var2, x2);

		boolean expected1Found = false;
		boolean expected2Found = false;
		boolean expected3Found = false;

		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertTrue(    b1.equals(expected1)
		            || b1.equals(expected2)
		            || b1.equals(expected3) );
		if ( b1.equals(expected1) ) expected1Found = true;
		if ( b1.equals(expected2) ) expected2Found = true;
		if ( b1.equals(expected3) ) expected3Found = true;

		assertTrue( it.hasNext() );
		final Binding b2 = it.next().asJenaBinding();
		assertTrue(    b2.equals(expected1)
		            || b2.equals(expected2)
		            || b2.equals(expected3) );
		if ( b2.equals(expected1) ) expected1Found = true;
		if ( b2.equals(expected2) ) expected2Found = true;
		if ( b2.equals(expected3) ) expected3Found = true;

		assertTrue( it.hasNext() );
		final Binding b3 = it.next().asJenaBinding();
		assertTrue(    b3.equals(expected1)
		            || b3.equals(expected2)
		            || b3.equals(expected3) );
		if ( b3.equals(expected1) ) expected1Found = true;
		if ( b3.equals(expected2) ) expected2Found = true;
		if ( b3.equals(expected3) ) expected3Found = true;

		assertFalse( it.hasNext() );

		assertTrue( expected1Found );
		assertTrue( expected2Found );
		assertTrue( expected3Found );
	}

	protected void _tpWithEmptyInput( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");

		final List<SolutionMapping> input = new ArrayList<>();

		final TriplePattern tp = new TriplePatternImpl(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var2);
				return set;
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return new HashSet<>();
			}
		}, useOuterJoinSemantics);

		assertFalse( it.hasNext() );
	}

	protected void _tpWithEmptySolutionMappingAsInput( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping() ); // empty solution mapping

		final TriplePattern tp = new TriplePatternImpl(var1,p,var2);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(s1,p,o1) );
		dataForMember.add( Triple.create(s2,p,o1) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				return new HashSet<>();
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return new HashSet<>();
			}
		}, useOuterJoinSemantics);

		// checking
		final Set<Binding> result = new HashSet<>();

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertFalse( it.hasNext() );

		boolean b1Found = false;
		boolean b2Found = false;
		for ( final Binding b : result ) {
			assertEquals( 2, b.size() );
			assertEquals( "http://example.org/o1", b.get(var2).getURI() );

			if ( b.get(var1).getURI().equals("http://example.org/s1") ) {
				b1Found = true;
			}
			else if ( b.get(var1).getURI().equals("http://example.org/s2") ) {
				b2Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
	}

	protected void _tpWithEmptyResponses( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x1")) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/x2")) );

		final Node p = NodeFactory.createURI("http://example.org/p");
		final TriplePattern tp = new TriplePatternImpl(var1,p,var2);

		final Graph dataForMember = GraphFactory.createGraphMem();

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var1);
				return set;
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return new HashSet<>();
			}
		}, useOuterJoinSemantics);

		// checking
		if ( useOuterJoinSemantics ) {
			final Set<Binding> result = new HashSet<>();

			assertTrue( it.hasNext() );
			result.add( it.next().asJenaBinding() );

			assertTrue( it.hasNext() );
			result.add( it.next().asJenaBinding() );

			assertFalse( it.hasNext() );

			boolean b1Found = false;
			boolean b2Found = false;
			for ( final Binding b : result ) {
				assertEquals( 1, b.size() );

				if ( b.get(var1).getURI().equals("http://example.org/x1") ) {
					b1Found = true;
				}
				else if ( b.get(var1).getURI().equals("http://example.org/x2") ) {
					b2Found = true;
				}
				else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			}

			assertTrue(b1Found);
			assertTrue(b2Found);
		}
		else {  // useOuterJoinSemantics == false
			assertFalse( it.hasNext() );
		}
	}

	protected void _tpWithIllegalBNodeJoin( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");

		final Node p      = NodeFactory.createURI("http://example.org/p");
		final Node uri    = NodeFactory.createURI("http://example.org/x1");
		final Node bnode1 = NodeFactory.createBlankNode();
		final Node bnode2 = NodeFactory.createBlankNode();

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, bnode1) );

		final TriplePattern tp = new TriplePatternImpl(var1, p, uri);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(bnode2, p, uri) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() { return Set.of(var1); }

			@Override
			public Set<Var> getPossibleVariables() { return Set.of(); }
		}, useOuterJoinSemantics);

		// checking
		if ( useOuterJoinSemantics ) {
			assertTrue( it.hasNext() );

			final Binding b = it.next().asJenaBinding();
			assertEquals( 1, b.size() );
			assertTrue( b.get(var1).isBlank() );

			assertFalse( it.hasNext() );
		}
		else { // useOuterJoinSemantics == false
			assertFalse( it.hasNext() );
		}
	}

	protected void _tpWithSpuriousDuplicates( final boolean useOuterJoinSemantics ) throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		final Node o2 = NodeFactory.createURI("http://example.org/o2");
		
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, s1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var1, s1, var2, o1) );

		final TriplePattern tp = new TriplePatternImpl(var1,p,var2);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(s1,p,o1) );
		dataForMember.add( Triple.create(s2,p,o2) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				return Collections.singleton(var1);
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return Collections.singleton(var2);
			}
		}, useOuterJoinSemantics);

		// checking
		assertTrue( it.hasNext() );

		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 2, b1.size() );
		assertEquals( s1, b1.get(var1) );
		assertEquals( o1, b1.get(var2) );

		assertTrue( it.hasNext() );

		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 2, b2.size() );
		assertEquals( s1, b2.get(var1) );
		assertEquals( o1, b2.get(var2) );

		assertFalse( it.hasNext() );
	}



	protected Iterator<SolutionMapping> runTest(
			final List<SolutionMapping> input,
			final Graph dataForMember,
			final TriplePattern tp,
			final ExpectedVariables expectedVariables,
			final boolean useOuterJoinSemantics ) throws ExecutionException
	{
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();
		final ExecutionContext execCxt = new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return getExecutorServiceForTest(); }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
		};
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final MemberType fm = createFedMemberForTest(dataForMember);

		final UnaryExecutableOp op = createExecOpForTest(tp, fm, expectedVariables, useOuterJoinSemantics);

		for ( final SolutionMapping sm : input ) {
			op.process(sm, sink, execCxt);
		}
		op.concludeExecution(sink, execCxt);

		return sink.getCollectedSolutionMappings().iterator();
	}

	protected abstract MemberType createFedMemberForTest( Graph dataForMember );

	protected abstract ExecutorService getExecutorServiceForTest();

	protected abstract UnaryExecutableOp createExecOpForTest( TriplePattern tp,
	                                                          MemberType fm,
	                                                          ExpectedVariables expectedVariables,
	                                                          boolean useOuterJoinSemantics );
}
