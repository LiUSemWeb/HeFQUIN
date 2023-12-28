package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
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

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessManager;
import se.liu.ida.hefquin.engine.federation.access.TriplePatternRequest;
import se.liu.ida.hefquin.engine.federation.access.impl.req.TriplePatternRequestImpl;
import se.liu.ida.hefquin.engine.federation.catalog.FederationCatalog;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.executable.IntermediateResultBlock;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.GenericIntermediateResultBlockImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryproc.ExecutionContext;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

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
	public void twoOptsWithJoinPartnersInBoth() throws ExecutionException {
		final Var var0 = Var.alloc("v0"); // used as join variable
		final Var var1 = Var.alloc("v1"); // used in first optional triple pattern
		final Var var2 = Var.alloc("v2"); // used in second optional triple pattern

		final Node p1  = NodeFactory.createURI("http://example.org/p1"); // used in first optional triple pattern
		final Node p2  = NodeFactory.createURI("http://example.org/p2"); // used in second optional triple pattern
		final Node x1  = NodeFactory.createURI("http://example.org/x1"); // used as join value
		final Node y1  = NodeFactory.createURI("http://example.org/y1"); // object of triple that matches first optional triple pattern
		final Node z1  = NodeFactory.createURI("http://example.org/z1"); // object of triple that matches second optional triple pattern

		// create input to the operator (as would be provided by the evaluation of the non-optional part)
		final GenericIntermediateResultBlockImpl input = new GenericIntermediateResultBlockImpl();
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
			final IntermediateResultBlock input,
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
		};
		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();

		final SPARQLEndpoint fm1 = createFedMemberForTest(dataForMember1);
		final SPARQLEndpoint fm2 = ( dataForMember2 == null ) ? null : createFedMemberForTest(dataForMember2);

		final UnaryExecutableOp op = createExecOpForTest(expectedInputVariables, tp1, fm1, tp2, fm2);
		op.process(input, sink, execCxt);
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
			return new ExecOpParallelMultiwayLeftJoin( false, expectedInputVariables, reqOp1 );
		}

		final TriplePatternRequest req2 = new TriplePatternRequestImpl(tp2);
		final LogicalOpRequest<?,?> reqOp2 = new LogicalOpRequest<>(fm2, req2);

		if ( tp3 == null ) {
			return new ExecOpParallelMultiwayLeftJoin( false, expectedInputVariables, reqOp1, reqOp2 );
		}

		final TriplePatternRequest req3 = new TriplePatternRequestImpl(tp3);
		final LogicalOpRequest<?,?> reqOp3 = new LogicalOpRequest<>(fm3, req3);

		return new ExecOpParallelMultiwayLeftJoin( false, expectedInputVariables, reqOp1, reqOp2, reqOp3 );
	}

}
