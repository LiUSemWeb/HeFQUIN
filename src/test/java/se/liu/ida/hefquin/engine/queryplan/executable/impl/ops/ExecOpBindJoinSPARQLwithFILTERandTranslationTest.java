package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.OWL;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.impl.VocabularyMappingImpl;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.queryplan.ExpectedVariables;
import se.liu.ida.hefquin.engine.queryproc.ExecutionException;

public class ExecOpBindJoinSPARQLwithFILTERandTranslationTest extends TestsForTPAddAlgorithmsWithTranslation<SPARQLEndpoint>{

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
	public void tpWithEmptySolutionMappingAsInput() throws ExecutionException {
		_tpWithEmptySolutionMappingAsInput();
	}

	@Test
	public void tpWithEmptyResponses() throws ExecutionException {
		_tpWithEmptyResponses();
	}

	@Override
	protected SPARQLEndpoint createFedMemberForTest(Graph dataForMember) {
		return new SPARQLEndpointWithVocabularyMappingForTest("http://example.org/sparql", dataForMember, createVocabularyMappingForTests());
	}

	@Override
	protected UnaryExecutableOp createExecOpForTest(TriplePattern tp, SPARQLEndpoint fm,
													final ExpectedVariables expectedVariables) {
		return new ExecOpBindJoinSPARQLwithFILTERandTranslation(tp, fm);
	}
	
	public static VocabularyMapping createVocabularyMappingForTests() {
		final Set<org.apache.jena.graph.Triple> mappingTriples = new HashSet<>();
		//Equality
		/*
		final Node s = NodeFactory.createURI("http://example.org/x1");
		final Node p = OWL.sameAs.asNode();
		final Node o  = NodeFactory.createURI("http://example.org/a");
		*/
		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = OWL.sameAs.asNode();
		final Node o  = NodeFactory.createURI("http://example.org/a");

		mappingTriples.add(new org.apache.jena.graph.Triple(s, p, o));

		return new VocabularyMappingImpl(mappingTriples);
	}
}
