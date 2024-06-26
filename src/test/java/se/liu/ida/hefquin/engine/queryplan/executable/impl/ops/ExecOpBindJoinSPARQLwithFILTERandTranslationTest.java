package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.mappings.impl.VocabularyMappingWrappingImpl;
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
		final boolean useOuterJoinSemantics = false;
		return new ExecOpBindJoinSPARQLwithFILTERandTranslation(tp, fm, useOuterJoinSemantics, false);
	}
	
	public static VocabularyMapping createVocabularyMappingForTests() {
		final Graph g = GraphFactory.createDefaultGraph();

		//Equality
		final Node x1  = NodeFactory.createURI("http://example.org/x1");
		Node p = OWL.sameAs.asNode();
		final Node a = NodeFactory.createURI("http://example.org/a");
		g.add(x1, p, a);

		final Node y1  = NodeFactory.createURI("http://example.org/y1");
		p = OWL.equivalentClass.asNode();
		final Node b = NodeFactory.createURI("http://example.org/b");
		g.add(y1, p, b);

		final Node x2  = NodeFactory.createURI("http://example.org/x2");
		p = OWL.sameAs.asNode();
		final Node c = NodeFactory.createURI("http://example.org/c");
		g.add(x2, p, c);

		final Node y2  = NodeFactory.createURI("http://example.org/y2");
		p = OWL.equivalentClass.asNode();
		final Node d = NodeFactory.createURI("http://example.org/d");
		g.add(y2, p, d);

		final Node s1 = NodeFactory.createURI("http://example.org/s1");
		p = OWL.sameAs.asNode();
		final Node global1 = NodeFactory.createURI("http://example.org/g1");
		g.add(s1, p, global1);

		final Node s2 = NodeFactory.createURI("http://example.org/s2");
		final Node global2 = NodeFactory.createURI("http://example.org/g2");
		g.add(s2, p, global2);

		final Node o1 = NodeFactory.createURI("http://example.org/o1");
		final Node global3 = NodeFactory.createURI("http://example.org/g3");
		g.add(o1, p, global3);

		return new VocabularyMappingWrappingImpl(g);
	}
}
