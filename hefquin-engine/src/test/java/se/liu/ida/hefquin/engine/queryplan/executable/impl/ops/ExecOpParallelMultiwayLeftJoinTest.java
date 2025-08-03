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
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.federation.catalog.FederationCatalog;

public class ExecOpParallelMultiwayLeftJoinTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>
{
	protected static ExecutorService execService;

	@BeforeClass
	public static void createExecService() {
		execService = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void tearDownExecService() {
		execService.shutdownNow();
		try {
			execService.awaitTermination(500L, TimeUnit.MILLISECONDS);
		}
		catch ( final InterruptedException ex )  {
			System.err.println("Terminating the thread pool was interrupted." );
			ex.printStackTrace();
		}
	}


	@Test
	public void tpWithJoinOnObject() throws ExecutionException {
		_tpWithJoinOnObject(true);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(true);
	}

	@Test
	public void tpWithoutJoinVariable() throws ExecutionException {
		_tpWithoutJoinVariable(true);
	}

	@Test
	public void tpWithEmptyInput() throws ExecutionException {
		_tpWithEmptyInput(true);
	}

// This test does not make much sense for the ExecOpParallelMultiwayLeftJoin
//	@Test
//	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException {
//		_tpWithEmptySolutionMappingAsInput(true);
//	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses(true);
	}

	@Test
	public void tpWithIllegalBNodeJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(true);
	}

	@Test
	public void twoOptsWithNoJoinPartners() throws ExecutionException {
		// No matching join-values in either.

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node x2  = NodeFactory.createURI("http://example.org/x2"); // used as incorrect value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x2,p1,y1) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x2,p2,z1) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 1, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var0).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void twoOptsWithOneJoinPartnerInBoth() throws ExecutionException {
		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				return Collections.singleton(var0);
			}

			@Override public Set<Var> getPossibleVariables() {
				return Collections.emptySet();
			}
		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 3, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var0).getURI() );
		assertEquals( "http://example.org/y1", b1.get(var1).getURI() );
		assertEquals( "http://example.org/z1", b1.get(var2).getURI() );

		assertFalse( it.hasNext() );
	}
	
	@Test
	public void twoOptsWithOneJoinPartnerInOne() throws ExecutionException {
		// Only one of the two OPTIONAL has a matching join-value.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node x2  = NodeFactory.createURI("http://example.org/x2"); // used as an incorrect join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x2,p2,z1) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 2, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var0).getURI() );
		assertEquals( "http://example.org/y1", b1.get(var1).getURI() );

		assertFalse( it.hasNext() );
	}
	
	@Test
	public void twoOptsWithOneJoinPartnerInOneMirrored() throws ExecutionException {
		// Same as the above, but it is the other one.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node x2  = NodeFactory.createURI("http://example.org/x2"); // used as an incorrect join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x2,p1,y1) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		assertTrue( it.hasNext() );
		final Binding b1 = it.next().asJenaBinding();
		assertEquals( 2, b1.size() );
		assertEquals( "http://example.org/x1", b1.get(var0).getURI() );
		assertEquals( "http://example.org/z1", b1.get(var2).getURI() );

		assertFalse( it.hasNext() );
	}
	

	
	@Test
	public void twoOptsWithIdenticalNonOptionals() throws ExecutionException {
		// Contains multiple solution mappings with the same join-value as
		// an input, as well as one mapping with a join-value which finds
		// no matches in the OPTIONALs.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern
		final Var var3 = Var.alloc("v3"); // used as non-join variable

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node x2  = NodeFactory.createURI("http://example.org/x2"); // incorrect join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node y2  = NodeFactory.createURI("http://example.org/y2");
		final Node y3  = NodeFactory.createURI("http://example.org/y3");
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern
		final Node z2  = NodeFactory.createURI("http://example.org/z2");
		final Node z3  = NodeFactory.createURI("http://example.org/z3");
		final Node w1  = NodeFactory.createURI("http://example.org/w1"); // used as secondary value to distinguish those with identical join value
		final Node w2  = NodeFactory.createURI("http://example.org/w2");
		final Node w3  = NodeFactory.createURI("http://example.org/w3");
		final Node w4  = NodeFactory.createURI("http://example.org/w4");
		
		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1, var3, w1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1, var3, w2) );
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1, var3, w3) );
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x2, var3, w4) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );
		dataForMember1.add( Triple.create(x1,p1,y2) );
		dataForMember1.add( Triple.create(x1,p1,y3) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );
		dataForMember2.add( Triple.create(x1,p2,z2) );
		dataForMember2.add( Triple.create(x1,p2,z3) );


		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		final Set<Binding> result = new HashSet<>();
		
		
		// All w1s
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		
		// All w2s
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		
		// All w3s
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		
		// The lone w4
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		// There should be no more.
		assertFalse( it.hasNext() );

		boolean b111Found = false;
		boolean b112Found = false;
		boolean b113Found = false;
		boolean b121Found = false;
		boolean b122Found = false;
		boolean b123Found = false;
		boolean b131Found = false;
		boolean b132Found = false;
		boolean b133Found = false;

		boolean b211Found = false;
		boolean b212Found = false;
		boolean b213Found = false;
		boolean b221Found = false;
		boolean b222Found = false;
		boolean b223Found = false;
		boolean b231Found = false;
		boolean b232Found = false;
		boolean b233Found = false;

		boolean b311Found = false;
		boolean b312Found = false;
		boolean b313Found = false;
		boolean b321Found = false;
		boolean b322Found = false;
		boolean b323Found = false;
		boolean b331Found = false;
		boolean b332Found = false;
		boolean b333Found = false;
		
		for ( final Binding b : result ) {
			if ( b.get(var3).getURI().equals("http://example.org/w1") ) {
				assertEquals( 4, b.size() );
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b111Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b112Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b113Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b121Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b122Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b123Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b131Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b132Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b133Found = true;
					}
				} else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			} else if ( b.get(var3).getURI().equals("http://example.org/w2") ) {
				assertEquals( 4, b.size() );
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b211Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b212Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b213Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b221Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b222Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b223Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b231Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b232Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b233Found = true;
					}
				} else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			} else if ( b.get(var3).getURI().equals("http://example.org/w3") ) {
				assertEquals( 4, b.size() );
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b311Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b312Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b313Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b321Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b322Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b323Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b331Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b332Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b333Found = true;
					}
				} else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			} else if ( b.get(var3).getURI().equals("http://example.org/w4") ) {
				assertEquals( 2, b.size() );
				assertEquals( "http://example.org/x2", b.get(var0).getURI() );
			}
		}

		assertTrue(b111Found);
		assertTrue(b112Found);
		assertTrue(b113Found);
		assertTrue(b121Found);
		assertTrue(b122Found);
		assertTrue(b123Found);
		assertTrue(b131Found);
		assertTrue(b132Found);
		assertTrue(b133Found);


		assertTrue(b211Found);
		assertTrue(b212Found);
		assertTrue(b213Found);
		assertTrue(b221Found);
		assertTrue(b222Found);
		assertTrue(b223Found);
		assertTrue(b231Found);
		assertTrue(b232Found);
		assertTrue(b233Found);
		
		assertTrue(b311Found);
		assertTrue(b312Found);
		assertTrue(b313Found);
		assertTrue(b321Found);
		assertTrue(b322Found);
		assertTrue(b323Found);
		assertTrue(b331Found);
		assertTrue(b332Found);
		assertTrue(b333Found);
	}
	
	@Test
	public void twoOptsWithMultiplePartnersInBoth() throws ExecutionException {
		// Multiple triples in both OPTIONAL clauses have matching join-values.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern in x1,x2,x3
		final Node y2  = NodeFactory.createURI("http://example.org/y2");
		final Node y3  = NodeFactory.createURI("http://example.org/y3");
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern
		final Node z2  = NodeFactory.createURI("http://example.org/z2");
		final Node z3  = NodeFactory.createURI("http://example.org/z3");

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );
		dataForMember1.add( Triple.create(x1,p1,y2) );
		dataForMember1.add( Triple.create(x1,p1,y3) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );
		dataForMember2.add( Triple.create(x1,p2,z2) );
		dataForMember2.add( Triple.create(x1,p2,z3) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		final Set<Binding> result = new HashSet<>();

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		assertFalse( it.hasNext() );

		boolean b11Found = false;
		boolean b12Found = false;
		boolean b13Found = false;
		boolean b21Found = false;
		boolean b22Found = false;
		boolean b23Found = false;
		boolean b31Found = false;
		boolean b32Found = false;
		boolean b33Found = false;
		for ( final Binding b : result ) {
			assertEquals( 3, b.size() );

			if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
				if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
					b11Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
					b12Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
					b13Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				}
			} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
				if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
					b21Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
					b22Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
					b23Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				}
			} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
				if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
					b31Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
					b32Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
					b33Found = true;
					assertEquals( 3, b.size() );
					assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				}
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b11Found);
		assertTrue(b12Found);
		assertTrue(b13Found);
		assertTrue(b21Found);
		assertTrue(b22Found);
		assertTrue(b23Found);
		assertTrue(b31Found);
		assertTrue(b32Found);
		assertTrue(b33Found);
	}
	
	@Test
	public void twoOptsWithMultiplePartnersInBothAndMultipleNonOptionals() throws ExecutionException {
		// The most complicated and thorough test. Multiple triples in both
		// OPTIONAL clauses have matching join-values. There are several
		// solution mappings as input, all with different join-values, all
		// of which match different triples from the OPTIONAL clauses but
		// not all of whom has any matches.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node x2  = NodeFactory.createURI("http://example.org/x2"); // used as another join value
		final Node x3  = NodeFactory.createURI("http://example.org/x3"); // used as another join value
		final Node x4  = NodeFactory.createURI("http://example.org/x4"); // used as an incorrect join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern in x1,x2,x3
		final Node y2  = NodeFactory.createURI("http://example.org/y2"); // for x1 and x2
		final Node y3  = NodeFactory.createURI("http://example.org/y3"); // for x1 and x4
		final Node y4  = NodeFactory.createURI("http://example.org/y4"); // for x4 only
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern
		final Node z2  = NodeFactory.createURI("http://example.org/z2");
		final Node z3  = NodeFactory.createURI("http://example.org/z3");
		final Node z4  = NodeFactory.createURI("http://example.org/z4");

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x2) );
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x3) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );
		dataForMember1.add( Triple.create(x1,p1,y2) );
		dataForMember1.add( Triple.create(x1,p1,y3) );
		dataForMember1.add( Triple.create(x2,p1,y1) );
		dataForMember1.add( Triple.create(x2,p1,y2) );
		dataForMember1.add( Triple.create(x3,p1,y1) );
		dataForMember1.add( Triple.create(x4,p1,y3) );
		dataForMember1.add( Triple.create(x4,p1,y4) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );
		dataForMember2.add( Triple.create(x1,p2,z2) );
		dataForMember2.add( Triple.create(x1,p2,z3) );
		dataForMember2.add( Triple.create(x2,p2,z1) );
		dataForMember2.add( Triple.create(x2,p2,z2) );
		dataForMember2.add( Triple.create(x3,p2,z1) );
		dataForMember2.add( Triple.create(x4,p2,z3) );
		dataForMember2.add( Triple.create(x4,p2,z4) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
		final Set<Binding> result = new HashSet<>();
		
		
		// All x1s
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		// All x2s
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );
		
		// The x3s
		assertTrue( it.hasNext() );
		result.add( it.next().asJenaBinding() );

		// There should be no more.
		assertFalse( it.hasNext() );

		boolean b111Found = false;
		boolean b112Found = false;
		boolean b113Found = false;
		boolean b114Found = false;
		boolean b121Found = false;
		boolean b122Found = false;
		boolean b123Found = false;
		boolean b124Found = false;
		boolean b131Found = false;
		boolean b132Found = false;
		boolean b133Found = false;
		boolean b134Found = false;
		boolean b141Found = false;
		boolean b142Found = false;
		boolean b143Found = false;
		boolean b144Found = false;

		boolean b211Found = false;
		boolean b212Found = false;
		boolean b213Found = false;
		boolean b214Found = false;
		boolean b221Found = false;
		boolean b222Found = false;
		boolean b223Found = false;
		boolean b224Found = false;
		boolean b231Found = false;
		boolean b232Found = false;
		boolean b233Found = false;
		boolean b234Found = false;
		boolean b241Found = false;
		boolean b242Found = false;
		boolean b243Found = false;
		boolean b244Found = false;

		boolean b311Found = false;
		boolean b312Found = false;
		boolean b313Found = false;
		boolean b314Found = false;
		boolean b321Found = false;
		boolean b322Found = false;
		boolean b323Found = false;
		boolean b324Found = false;
		boolean b331Found = false;
		boolean b332Found = false;
		boolean b333Found = false;
		boolean b334Found = false;
		boolean b341Found = false;
		boolean b342Found = false;
		boolean b343Found = false;
		boolean b344Found = false;

		boolean b411Found = false;
		boolean b412Found = false;
		boolean b413Found = false;
		boolean b414Found = false;
		boolean b421Found = false;
		boolean b422Found = false;
		boolean b423Found = false;
		boolean b424Found = false;
		boolean b431Found = false;
		boolean b432Found = false;
		boolean b433Found = false;
		boolean b434Found = false;
		boolean b441Found = false;
		boolean b442Found = false;
		boolean b443Found = false;
		boolean b444Found = false;
		for ( final Binding b : result ) {
			assertEquals( 3, b.size() );
			if ( b.get(var0).getURI().equals("http://example.org/x1") ) {
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b111Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b112Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b113Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b114Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b121Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b122Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b123Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b124Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b131Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b132Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b133Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b134Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y4") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b141Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b142Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b143Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b144Found = true;
					}
				}
				else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			} else if ( b.get(var0).getURI().equals("http://example.org/x2") ) {
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b211Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b212Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b213Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b214Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b221Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b222Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b223Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b224Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b231Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b232Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b233Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b234Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y4") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b241Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b242Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b243Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b244Found = true;
					}
				}
				else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			} else if ( b.get(var0).getURI().equals("http://example.org/x3") ) {
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b311Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b312Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b313Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b314Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b321Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b322Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b323Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b324Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b331Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b332Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b333Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b334Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y4") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b341Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b342Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b343Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b344Found = true;
					}
				}
				else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			} else if ( b.get(var0).getURI().equals("http://example.org/x4") ) {
				if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b411Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b412Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b413Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b414Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b421Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b422Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b423Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b424Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b431Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b432Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b433Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b434Found = true;
					}
				} else if (b.get(var1).getURI().equals("http://example.org/y4") ) {
					if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
						b441Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z2") ) {
						b442Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z3") ) {
						b443Found = true;
					} else if ( b.get(var2).getURI().equals("http://example.org/z4") ) {
						b444Found = true;
					}
				}
				else {
					fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
				}
			}
		}

		assertTrue(b111Found);
		assertTrue(b112Found);
		assertTrue(b113Found);
		assertFalse(b114Found);
		assertTrue(b121Found);
		assertTrue(b122Found);
		assertTrue(b123Found);
		assertFalse(b124Found);
		assertTrue(b131Found);
		assertTrue(b132Found);
		assertTrue(b133Found);
		assertFalse(b134Found);
		assertFalse(b141Found);
		assertFalse(b142Found);
		assertFalse(b143Found);
		assertFalse(b144Found);

		assertTrue(b211Found);
		assertTrue(b212Found);
		assertFalse(b213Found);
		assertFalse(b214Found);
		assertTrue(b221Found);
		assertTrue(b222Found);
		assertFalse(b223Found);
		assertFalse(b224Found);
		assertFalse(b231Found);
		assertFalse(b232Found);
		assertFalse(b233Found);
		assertFalse(b234Found);
		assertFalse(b241Found);
		assertFalse(b242Found);
		assertFalse(b243Found);
		assertFalse(b244Found);
		

		assertTrue(b311Found);
		assertFalse(b312Found);
		assertFalse(b313Found);
		assertFalse(b314Found);
		assertFalse(b321Found);
		assertFalse(b322Found);
		assertFalse(b323Found);
		assertFalse(b324Found);
		assertFalse(b331Found);
		assertFalse(b332Found);
		assertFalse(b333Found);
		assertFalse(b334Found);
		assertFalse(b341Found);
		assertFalse(b342Found);
		assertFalse(b343Found);
		assertFalse(b344Found);
		
		assertFalse(b411Found);
		assertFalse(b412Found);
		assertFalse(b413Found);
		assertFalse(b414Found);
		assertFalse(b421Found);
		assertFalse(b422Found);
		assertFalse(b423Found);
		assertFalse(b424Found);
		assertFalse(b431Found);
		assertFalse(b432Found);
		assertFalse(b433Found);
		assertFalse(b434Found);
		assertFalse(b441Found);
		assertFalse(b442Found);
		assertFalse(b443Found);
		assertFalse(b444Found);
	}
	
	@Test
	public void twoOptsWithMultiplePartnersInOne() throws ExecutionException {
		// Multiple triples in one of the OPTIONAL clauses match.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node y2  = NodeFactory.createURI("http://example.org/y2");
		final Node y3  = NodeFactory.createURI("http://example.org/y3");
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );
		dataForMember1.add( Triple.create(x1,p1,y2) );
		dataForMember1.add( Triple.create(x1,p1,y3) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
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

			if ( b.get(var1).getURI().equals("http://example.org/y1") ) {
				b1Found = true;
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				assertEquals( "http://example.org/z1", b.get(var2).getURI() );
			} else if (b.get(var1).getURI().equals("http://example.org/y2") ) {
				b2Found = true;
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				assertEquals( "http://example.org/z1", b.get(var2).getURI() );
			} else if (b.get(var1).getURI().equals("http://example.org/y3") ) {
				b3Found = true;
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				assertEquals( "http://example.org/z1", b.get(var2).getURI() );
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);
	}

	@Test
	public void twoOptsWithMultiplePartnersInOneMirrored() throws ExecutionException {
		// Same as above, but it is the other OPTIONAL clause.
		// Added by https://github.com/LiUSemWeb/HeFQUIN/pull/221

		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern
		final Node z2  = NodeFactory.createURI("http://example.org/z2");
		final Node z3  = NodeFactory.createURI("http://example.org/z3");

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var0, x1) );

		final ExpectedVariables expectedInputVariables = new ExpectedVariables() {
			@Override public Set<Var> getCertainVariables() {
				final Set<Var> set = new HashSet<>();
				set.add(var0);
				return set;
			}

			@Override public Set<Var> getPossibleVariables() {
				final Set<Var> set = new HashSet<>();
				return set;
			}

		};

		// set up everything for the first optional part
		final TriplePattern tp1 = new TriplePatternImpl(var0, p1, var1);

		final Graph dataForMember1 = GraphFactory.createGraphMem();
		dataForMember1.add( Triple.create(x1,p1,y1) );

		// set up everything for the second optional part
		final TriplePattern tp2 = new TriplePatternImpl(var0, p2, var2);

		final Graph dataForMember2 = GraphFactory.createGraphMem();
		dataForMember2.add( Triple.create(x1,p2,z1) );
		dataForMember2.add( Triple.create(x1,p2,z2) );
		dataForMember2.add( Triple.create(x1,p2,z3) );

		// execute the operator
		final Iterator<SolutionMapping> it = runTest(input, expectedInputVariables, dataForMember1, tp1, dataForMember2, tp2 );

		// verify that the outcome is as expected
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

			if ( b.get(var2).getURI().equals("http://example.org/z1") ) {
				b1Found = true;
				assertEquals( 3, b.size() );
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				assertEquals( "http://example.org/y1", b.get(var1).getURI() );
			} else if (b.get(var2).getURI().equals("http://example.org/z2") ) {
				b2Found = true;
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				assertEquals( "http://example.org/y1", b.get(var1).getURI() );
			} else if (b.get(var2).getURI().equals("http://example.org/z3") ) {
				b3Found = true;
				assertEquals( "http://example.org/x1", b.get(var0).getURI() );
				assertEquals( "http://example.org/y1", b.get(var1).getURI() );
			}
			else {
				fail( "Unexpected URI for ?v1: " + b.get(var1).getURI() );
			}
		}

		assertTrue(b1Found);
		assertTrue(b2Found);
		assertTrue(b3Found);
	}


	// ----------helper methods ------------

	@Override
	protected SPARQLEndpoint createFedMemberForTest(Graph dataForMember) {
		return new SPARQLEndpointForTest(dataForMember);
	}

	@Override
	protected ExecutorService getExecutorServiceForTest() {
		return execService;
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp,
	                                                 final SPARQLEndpoint fm,
	                                                 final ExpectedVariables expectedVariables,
	                                                 final boolean useOuterJoinSemantics ) {
		if ( ! useOuterJoinSemantics )
			throw new UnsupportedOperationException();

		return createExecOpForTest(expectedVariables, tp, fm, null, null, null, null);
	}

	protected Iterator<SolutionMapping> runTest(
			final List<SolutionMapping> input,
			final ExpectedVariables expectedInputVariables,
			final Graph dataForMember1,
			final TriplePattern tp1,
			final Graph dataForMember2,
			final TriplePattern tp2 ) throws ExecutionException
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

		final SPARQLEndpoint fm1 = createFedMemberForTest(dataForMember1);
		final SPARQLEndpoint fm2 = ( dataForMember2 == null ) ? null : createFedMemberForTest(dataForMember2);

		final UnaryExecutableOp op = createExecOpForTest(expectedInputVariables, tp1, fm1, tp2, fm2);
		for ( final SolutionMapping sm : input ) {
			op.process(sm, sink, execCxt);
		}
		op.concludeExecution(sink, execCxt);

		return sink.getCollectedSolutionMappings().iterator();
	}

	protected UnaryExecutableOp createExecOpForTest( final ExpectedVariables expectedInputVariables,
	                                                 final TriplePattern tp,
	                                                 final SPARQLEndpoint fm ) {
		return createExecOpForTest(expectedInputVariables, tp, fm, null, null);
	}

	/**
	 * The parameters tp2 and fm2 may be null; tp1 and fm1 not.
	 */
	protected UnaryExecutableOp createExecOpForTest( final ExpectedVariables expectedInputVariables,
	                                                 final TriplePattern tp1,
	                                                 final SPARQLEndpoint fm1,
	                                                 final TriplePattern tp2,
	                                                 final SPARQLEndpoint fm2 ) {
		return createExecOpForTest(expectedInputVariables, tp1, fm1, tp2, fm2, null, null);
	}

	/**
	 * The parameters tp2, fm2, tp3, and fm3 may be null; tp1 and fm1 not.
	 */
	protected UnaryExecutableOp createExecOpForTest( final ExpectedVariables expectedInputVariables,
	                                                 final TriplePattern tp1,
	                                                 final SPARQLEndpoint fm1,
	                                                 final TriplePattern tp2,
	                                                 final SPARQLEndpoint fm2,
	                                                 final TriplePattern tp3,
	                                                 final SPARQLEndpoint fm3 ) {
		final TriplePatternRequest req1 = new TriplePatternRequestImpl(tp1);
		final LogicalOpRequest<?,?> reqOp1 = new LogicalOpRequest<>(fm1, req1);

		if ( tp2 == null ) {
			return new ExecOpParallelMultiwayLeftJoin( false, null, expectedInputVariables, reqOp1 );
		}

		final TriplePatternRequest req2 = new TriplePatternRequestImpl(tp2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(fm2, req2);

		if ( tp3 == null ) {
			return new ExecOpParallelMultiwayLeftJoin( false, null, expectedInputVariables, reqOp1, reqOp2 );
		}

		final TriplePatternRequest req3 = new TriplePatternRequestImpl(tp3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>(fm3, req3);

		return new ExecOpParallelMultiwayLeftJoin( false, null, expectedInputVariables, reqOp1, reqOp2, reqOp3 );
	}

}
