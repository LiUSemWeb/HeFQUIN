package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public abstract class TestsForTPAddAlgorithmsWithTranslation<MemberType extends FederationMember> extends ExecOpTestBase {

	protected void _tpWithJoinOnObject() throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");
		final Node z3 = NodeFactory.createURI("http://example.org/z3");
		
		final Node ax1 = NodeFactory.createURI("http://example.org/a");
		final Node by1 = NodeFactory.createURI("http://example.org/b");
		final Node cx2 = NodeFactory.createURI("http://example.org/c");
		final Node dy2 = NodeFactory.createURI("http://example.org/d");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, ax1,
				var2, by1) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, cx2,
				var2, dy2) );

		final TriplePattern tp = new TriplePatternImpl(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );
		dataForMember.add( Triple.create(y1,p,z2) );
		dataForMember.add( Triple.create(y2,p,z3) );

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
		});

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

			if ( b.get(var1).getURI().equals("http://example.org/a") ) {
				assertEquals( "http://example.org/b", b.get(var2).getURI() );
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
			else if ( b.get(var1).getURI().equals("http://example.org/c") ) {
				assertEquals( "http://example.org/d", b.get(var2).getURI() );
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

	protected void _tpWithJoinOnSubjectAndObject() throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");
		
		final Node ax1 = NodeFactory.createURI("http://example.org/a");
		final Node by1 = NodeFactory.createURI("http://example.org/b");
		final Node cx2 = NodeFactory.createURI("http://example.org/c");
		final Node dy2 = NodeFactory.createURI("http://example.org/d");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, ax1,
				var2, by1) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, cx2,
				var2, dy2) );

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
		});

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

			if ( b.get(var1).getURI().equals("http://example.org/a") ) {
				assertEquals( "http://example.org/b", b.get(var2).getURI() );
				assertEquals( "http://example.org/z1", b.get(var3).getURI() );
				b1Found = true;
			}
			else if ( b.get(var1).getURI().equals("http://example.org/c") ) {
				assertEquals( "http://example.org/d", b.get(var2).getURI() );
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

	protected void _tpWithoutJoinVariable() throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node y2 = NodeFactory.createURI("http://example.org/y2");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");
		final Node z2 = NodeFactory.createURI("http://example.org/z2");
		
		final Node ax1 = NodeFactory.createURI("http://example.org/a");
		final Node cx2 = NodeFactory.createURI("http://example.org/c");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, ax1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var1, cx2) );

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
		});

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

	protected void _tpWithEmptyInput() throws ExecutionException {
		final Var var2 = Var.alloc("v2");
		final Var var3 = Var.alloc("v3");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node y1 = NodeFactory.createURI("http://example.org/y1");
		final Node z1 = NodeFactory.createURI("http://example.org/z1");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();

		final TriplePattern tp = new TriplePatternImpl(var2,p,var3);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(y1,p,z1) );

		final Iterator<SolutionMapping> it = runTest(input, dataForMember, tp, new ExpectedVariables() {
			@Override
			public Set<Var> getCertainVariables() {
				return new HashSet<>();
			}

			@Override
			public Set<Var> getPossibleVariables() {
				return new HashSet<>();
			}
		});

		assertFalse( it.hasNext() );
	}

	protected void _tpWithEmptySolutionMappingAsInput() throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
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
		});

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
			assertEquals( "http://example.org/g3", b.get(var2).getURI() );

			if ( b.get(var1).getURI().equals("http://example.org/g1") ) {
				b1Found = true;
			}
			else if ( b.get(var1).getURI().equals("http://example.org/g2") ) {
				b2Found = true;
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
	}

	protected void _tpWithEmptyResponses() throws ExecutionException {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/a")) );
		input.add( SolutionMappingUtils.createSolutionMapping(
				var1, NodeFactory.createURI("http://example.org/b")) );

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
		});

		assertFalse( it.hasNext() );
	}
	
	protected void _tpWithSpuriousDuplicates() {
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		final Node o2 = NodeFactory.createURI("http://example.org/o2");
		
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node b = NodeFactory.createURI("http://example.org/b");
		
		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, a) );
		input.add( SolutionMappingUtils.createSolutionMapping(var1, a, var2, b) );

		final TriplePattern tp = new TriplePatternImpl(var1,p,var2);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(s1,p,o1) );
		dataForMember.add( Triple.create(s2,p,o2) );

		assertThrows(IllegalArgumentException.class,  () -> {
			runTest(input, dataForMember, tp, new ExpectedVariables() {
				@Override
				public Set<Var> getCertainVariables() {
					final Set<Var> set = new HashSet<>();
					set.add(var1);
					return set;
				}

				@Override
				public Set<Var> getPossibleVariables() {
					final Set<Var> set = new HashSet<>();
					set.add(var2);
					return set;
				}
			});
		});
	}



	protected Iterator<SolutionMapping> runTest(
			final IntermediateResultBlock input,
			final Graph dataForMember,
			final TriplePattern tp,
			final ExpectedVariables expectedVariables) throws ExecutionException
	{
		final FederationAccessManager fedAccessMgr = new FederationAccessManagerForTest();
		final ExecutionContext execCxt = new ExecutionContext() {
			@Override public FederationCatalog getFederationCatalog() { return null; }
			@Override public FederationAccessManager getFederationAccessMgr() { return fedAccessMgr; }
			@Override public ExecutorService getExecutorServiceForPlanTasks() { return null; }
			@Override public boolean isExperimentRun() { return false; }
			@Override public boolean skipExecution() { return false; }
		};
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final MemberType fm = createFedMemberForTest(dataForMember);

		final UnaryExecutableOp op = createExecOpForTest(tp, fm, expectedVariables);
		op.process(input, sink, execCxt);
		op.concludeExecution(sink, execCxt);

		return sink.getCollectedSolutionMappings().iterator();
	}

	protected abstract MemberType createFedMemberForTest( Graph dataForMember );

	protected abstract UnaryExecutableOp createExecOpForTest(TriplePattern tp, MemberType fm, ExpectedVariables expectedVariables);

}
