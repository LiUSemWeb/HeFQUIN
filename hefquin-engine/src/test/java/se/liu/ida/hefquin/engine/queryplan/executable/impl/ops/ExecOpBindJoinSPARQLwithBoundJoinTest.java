package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
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
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

public class ExecOpBindJoinSPARQLwithBoundJoinTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>
{

	@Test
	public void tpWithMissingNonJoinVar_InnerJoin() throws ExecutionException {
		_tpWithMissingNonJoinVar(false);
	}

	@Test
	public void tpWithMissingNonJoinVars_OuterJoin() throws ExecutionException {
		_tpWithMissingNonJoinVar(true);
	}

	public void _tpWithMissingNonJoinVar( final boolean useOuterJoinSemantics )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, s) );

		final TriplePattern tp = new TriplePatternImpl(var1, p, o);

		final Graph dataForMember = GraphFactory.createGraphMem();
		dataForMember.add( Triple.create(s, p, o) );

		final Exception exception = assertThrows( IllegalArgumentException.class, () -> {
			runTest( input, dataForMember, tp, new ExpectedVariables() {
				@Override
				public Set<Var> getCertainVariables() { return Set.of(var1); }

				@Override
				public Set<Var> getPossibleVariables() { return Set.of(); }
			}, useOuterJoinSemantics );
		} );

		assertEquals( exception.getMessage(), "No suitable variable found for renaming" );
	}

	@Test
	public void tpWithJoinOnObject_InnerJoin() throws ExecutionException {
		_tpWithJoinOnObject(false);
	}

	@Test
	public void tpWithJoinOnObject_OuterJoin() throws ExecutionException {
		_tpWithJoinOnObject(true);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_InnerJoin() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(false);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject_OuterJoin() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(true);
	}

	@Test
	public void tpWithoutJoinVariable_InnerJoin() throws ExecutionException {
		_tpWithoutJoinVariable(false);
	}

	@Test
	public void tpWithoutJoinVariable_OuterJoin() throws ExecutionException {
		_tpWithoutJoinVariable(true);
	}

	@Test
	public void tpWithEmptyInput_InnerJoin() throws ExecutionException {
		_tpWithEmptyInput(false);
	}

	@Test
	public void tpWithEmptyInput_OuterJoin() throws ExecutionException {
		_tpWithEmptyInput(true);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_InnerJoin() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(false);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput_OuterJoin() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(true);
	}

	@Test
	public void tpWithEmptyResponses_InnerJoin() throws ExecutionException {
		_tpWithEmptyResponses(false);
	}

	@Test
	public void tpWithEmptyResponses_OuterJoin() throws ExecutionException {
		_tpWithEmptyResponses(true);
	}

	@Test
	public void tpWithIllegalBNodeJoin_InnerJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(false);
	}

	@Test
	public void tpWithIllegalBNodeJoin_OuterJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(true);
	}

	// The original version of _tpWithIllegalBNodeJoin has no non-join variable.
	protected void _tpWithIllegalBNodeJoin( final boolean useOuterJoinSemantics )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("p");

		final Node p      = NodeFactory.createURI("http://example.org/p");
		final Node uri    = NodeFactory.createURI("http://example.org/x1");
		final Node bnode1 = NodeFactory.createBlankNode();
		final Node bnode2 = NodeFactory.createBlankNode();

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, bnode1) );

		final TriplePattern tp = new TriplePatternImpl(var1, var2, uri);

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

	@Test
	public void tpWithSpuriousDuplicates_InnerJoin() throws ExecutionException {
		_tpWithSpuriousDuplicates(false);
	}

	@Test
	public void tpWithSpuriousDuplicates_OuterJoin() throws ExecutionException {
		_tpWithSpuriousDuplicates(true);
	}

	// The original version of _tpWithSpuriousDuplicates has no non-join variable.
	protected void _tpWithSpuriousDuplicates( final boolean useOuterJoinSemantics )
			throws ExecutionException
	{
		final Var var1 = Var.alloc("v1");
		final Var var2 = Var.alloc("v2");
		// var3 is the non-join var
		final Var var3 = Var.alloc("p");

		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		final Node o2 = NodeFactory.createURI("http://example.org/o2");

		final List<SolutionMapping> input = new ArrayList<>();
		input.add( SolutionMappingUtils.createSolutionMapping(var1, s1) );
		input.add( SolutionMappingUtils.createSolutionMapping(var1, s1, var2, o1) );

		final TriplePattern tp = new TriplePatternImpl(var1, var3, var2);

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
		assertEquals( 3, b1.size() );
		assertEquals( s1, b1.get(var1) );
		assertEquals( o1, b1.get(var2) );

		assertTrue( it.hasNext() );

		final Binding b2 = it.next().asJenaBinding();
		assertEquals( 3, b2.size() );
		assertEquals( s1, b2.get(var1) );
		assertEquals( o1, b2.get(var2) );

		assertFalse( it.hasNext() );
	}

	@Override
	protected SPARQLEndpoint createFedMemberForTest( final Graph dataForMember) {
		return new SPARQLEndpointForTest(dataForMember);
	}

	@Override
	protected ExecutorService getExecutorServiceForTest() {
		return null;
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest( final TriplePattern tp,
	                                                 final SPARQLEndpoint fm,
	                                                 final ExpectedVariables expectedVariables,
	                                                 final boolean useOuterJoinSemantics ) {
		return new ExecOpBindJoinSPARQLwithBoundJoin( tp,
		                                              fm,
		                                              expectedVariables,
		                                              useOuterJoinSemantics,
		                                              ExecOpBindJoinSPARQLwithBoundJoin.DEFAULT_BATCH_SIZE,
		                                              false );
	}

}
