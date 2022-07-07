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
import se.liu.ida.hefquin.engine.queryplan.executable.UnaryExecutableOp;
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

		
		
		final Node a = NodeFactory.createURI("http://example.org/a");
		Node p = OWL.sameAs.asNode();
		final Node x1  = NodeFactory.createURI("http://example.org/x1");
		mappingTriples.add(new org.apache.jena.graph.Triple(a, p, x1));
		
		final Node b = NodeFactory.createURI("http://example.org/b");
		p = OWL.equivalentClass.asNode();
		final Node y1  = NodeFactory.createURI("http://example.org/y1");
		mappingTriples.add(new org.apache.jena.graph.Triple(b, p, y1));
		
		final Node c = NodeFactory.createURI("http://example.org/c");
		p = OWL.sameAs.asNode();
		final Node x2  = NodeFactory.createURI("http://example.org/x2");
		mappingTriples.add(new org.apache.jena.graph.Triple(c, p, x2));
		
		final Node d = NodeFactory.createURI("http://example.org/d");
		p = OWL.equivalentClass.asNode();
		final Node y2  = NodeFactory.createURI("http://example.org/y2");
		mappingTriples.add(new org.apache.jena.graph.Triple(d, p, y2));
		
		final Node global1 = NodeFactory.createURI("http://example.org/g1");
		p = OWL.sameAs.asNode();
		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		mappingTriples.add(new org.apache.jena.graph.Triple(global1, p, s1));
		
		final Node global2 = NodeFactory.createURI("http://example.org/g2");
		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		mappingTriples.add(new org.apache.jena.graph.Triple(global2, p, s2));
		
		final Node global3 = NodeFactory.createURI("http://example.org/g3");
		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		mappingTriples.add(new org.apache.jena.graph.Triple(global3, p, o1));

		return new VocabularyMappingImpl(mappingTriples);
	}
}
