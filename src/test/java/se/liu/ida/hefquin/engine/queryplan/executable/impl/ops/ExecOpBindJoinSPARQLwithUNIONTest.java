package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpBindJoinSPARQLwithUNIONTest extends TestsForTPAddAlgorithms<SPARQLEndpoint> {
	
	@Test
	public void tpWithJoinOnObject() throws ExecutionException {
		_tpWithJoinOnObject();
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() throws ExecutionException {
		_tpWithJoinOnSubjectAndObject();
	}

	@Test
	public void tpWithoutJoinVariable() throws ExecutionException {
		_tpWithoutJoinVariable();
	}

	@Test
	public void tpWithEmptyInput() throws ExecutionException {
		_tpWithEmptyInput();
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException{
		_tpWithEmptySolutionMappingAsInput();
	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses();
	}

	@Test
	public void tpWithSpuriousDuplicates() throws ExecutionException {
		_tpWithSpuriousDuplicates();
	}

	@Override
	protected SPARQLEndpoint createFedMemberForTest( final Graph dataForMember) {
		return new SPARQLEndpointForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest(final TriplePattern tp, final SPARQLEndpoint fm,
													final ExpectedVariables expectedVariables) {
		final LogicalOpTPAdd tpadd = new LogicalOpTPAdd(fm, tp);
		final PhysicalOpBindJoinWithUNION bindjoin = new PhysicalOpBindJoinWithUNION(tpadd); 
		return bindjoin.createExecOp(expectedVariables);
	}

}
