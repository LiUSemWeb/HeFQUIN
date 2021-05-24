package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Graph;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUES;

public class ExecOpBindJoinSPARQLwithVALUESTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>{
	
	@Test
	public void tpWithJoinOnObject() {
		_tpWithJoinOnObject();
	}

	@Test
	public void tpWithJoinOnSubjectAndObject() {
		_tpWithJoinOnSubjectAndObject();
	}

	@Test
	public void tpWithoutJoinVariable() {
		_tpWithoutJoinVariable();
	}

	@Test
	public void tpWithEmptyInput() {
		_tpWithEmptyInput();
	}

	@Test
	public void tpWithEmptySolutionMappingAsInput() {
		_tpWithEmptySolutionMappingAsInput();
	}

	@Test
	public void tpWithEmptyResponses() {
		_tpWithEmptyResponses();
	}

	@Override
	protected SPARQLEndpoint createFedMemberForTest( final Graph dataForMember ) {
		return new SPARQLEndpointForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest(final TriplePattern tp, final SPARQLEndpoint fm,
													final ExpectedVariables expectedVariables) {
		final LogicalOpTPAdd tpAdd = new LogicalOpTPAdd(fm, tp);
		final PhysicalOpBindJoinWithVALUES physicalOp = new PhysicalOpBindJoinWithVALUES(tpAdd);
		return physicalOp.createExecOp(expectedVariables);
	}

}
