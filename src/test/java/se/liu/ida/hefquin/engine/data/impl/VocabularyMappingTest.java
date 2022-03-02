package se.liu.ida.hefquin.engine.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.Triple;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;

public class VocabularyMappingTest {

	@Test
	public void VocabularyMappingConstructorTest() {
		final Pair<Set<Triple>,List<SPARQLGraphPattern>> testData= CreateTestTriples();
		final Set<Triple> testTriples = testData.object1;
		final Set<org.apache.jena.graph.Triple> jenaTriples = new HashSet<>();
		for(Triple t : testTriples) {
			jenaTriples.add(t.asJenaTriple());
		}
		final VocabularyMappingImpl vm = new VocabularyMappingImpl(testTriples);
		
		final Node s = NodeFactory.createVariable("s");
		final Node p = NodeFactory.createVariable("p");
		final Node o = NodeFactory.createVariable("o");
		final org.apache.jena.graph.Triple testQuery = new TriplePatternImpl(s, p, o).asJenaTriple();
		final Set<org.apache.jena.graph.Triple> queryResults = new HashSet<>();
		final Iterator<org.apache.jena.graph.Triple> i = vm.getVocabularyMappingAsGraph().find(testQuery);
		while(i.hasNext()) {
			queryResults.add(i.next());
		}
		assertEquals(queryResults, jenaTriples);
	}
	
	@Test
	public void TranslateTriplePatternTest() {
		final Pair<Set<Triple>,List<SPARQLGraphPattern>> testData= CreateTestTriples();
		final Set<Triple> testTriples = testData.object1;
		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		final Node s = NodeFactory.createLiteral("s1");
		final Node p = NodeFactory.createURI(RDF.type.getURI());
		final Node o = NodeFactory.createLiteral("o1");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp);
		
		final SPARQLUnionPattern correct = new SPARQLUnionPatternImpl(testData.object2);	
		
		assertEquals(correct, translation);
	}
	
	public Pair<Set<Triple>,List<SPARQLGraphPattern>> CreateTestTriples(){
		final Set<Triple> testSet = new HashSet<>();
		
		Node s = NodeFactory.createLiteral("s1");
		Node p = OWL.sameAs.asNode();
		Node sRes = NodeFactory.createLiteral("s2");
		testSet.add(new TripleImpl(s, p, sRes));
		
		s = RDF.type.asNode();
		p = OWL.inverseOf.asNode();
		Node pRes = NodeFactory.createLiteral("Not type");
		testSet.add(new TripleImpl(s, p, pRes));
		
		s = NodeFactory.createLiteral("o1");
		p = OWL.equivalentClass.asNode();
		Node o = NodeFactory.createBlankNode();
		testSet.add(new TripleImpl(s, p, o));
		
		s = o;
		p = OWL.unionOf.asNode();
		Node o1Res = NodeFactory.createLiteral("o2");
		testSet.add(new TripleImpl(s, p, o1Res));
		
		Node o2Res = NodeFactory.createLiteral("o3");
		testSet.add(new TripleImpl(s, p, o2Res));
		
		final List<SPARQLGraphPattern> expectedRes = new ArrayList<>();
		expectedRes.add(new TriplePatternImpl(o2Res, pRes, sRes));
		expectedRes.add(new TriplePatternImpl(o1Res, pRes, sRes));
		
		Pair<Set<Triple>,List<SPARQLGraphPattern>> returnP = new Pair<Set<Triple>,List<SPARQLGraphPattern>>(testSet, expectedRes);
		return returnP;
	}
}
