package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.concurrent.ExecutorService;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;
import se.liu.ida.hefquin.federation.SPARQLEndpoint;

public class ExecOpIndexNestedLoopsJoinSPARQLTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>
{
	@Test
	public void tpWithJoinOnObject() throws ExecutionException {
		_tpWithJoinOnObject(false);
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject(false);
	}

	@Test
	public void tpWithoutJoinVariable() throws ExecutionException {
		_tpWithoutJoinVariable(false);
	}

	@Test
	public void tpWithEmptyInput() throws ExecutionException {
		_tpWithEmptyInput(false);
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput(false);
	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses(false);
	}

	@Test
	public void tpWithIllegalBNodeJoin() throws ExecutionException {
		_tpWithIllegalBNodeJoin(false);
	}


	@Override
	protected SPARQLEndpoint createFedMemberForTest( final Graph dataForMember ) {
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
		return new ExecOpIndexNestedLoopsJoinSPARQL(tp, fm, useOuterJoinSemantics, false);
	}
}
