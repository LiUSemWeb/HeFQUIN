package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
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

	@Test
	public void tpWithSpuriousDuplicates_InnerJoin() throws ExecutionException {
		_tpWithSpuriousDuplicates(false);
	}

	@Test
	public void tpWithSpuriousDuplicates_OuterJoin() throws ExecutionException {
		_tpWithSpuriousDuplicates(true);
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
		                                              false,
		                                              null );
	}
}
