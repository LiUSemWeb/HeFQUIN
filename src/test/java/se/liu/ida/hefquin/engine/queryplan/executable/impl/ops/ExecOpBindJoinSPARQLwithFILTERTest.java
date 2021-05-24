package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpTPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;

public class ExecOpBindJoinSPARQLwithFILTERTest extends TestsForTPAddAlgorithms<SPARQLEndpoint>{
	
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
	protected SPARQLEndpoint createFedMemberForTest(Graph dataForMember) {
		return new SPARQLEndpointForTest(dataForMember);
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest(TriplePattern tp, SPARQLEndpoint fm,
													final ExpectedVariables expectedVariables) {
		return new ExecOpBindJoinSPARQLwithFILTER(tp, fm);
	}

}
