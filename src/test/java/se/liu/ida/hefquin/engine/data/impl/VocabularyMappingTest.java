package se.liu.ida.hefquin.engine.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
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
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;

public class VocabularyMappingTest {

	@Test
	public void VocabularyMappingConstructorTest() {
		final Pair<Set<Triple>,Set<org.apache.jena.graph.Triple>> testData= CreateTestTriples();
		final Set<Triple> testTriples = testData.object1;
		final Set<org.apache.jena.graph.Triple> jenaTriples = new HashSet<>();
		for(final Triple t : testTriples) {
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
		final Pair<Set<Triple>,Set<org.apache.jena.graph.Triple>> testData= CreateTestTriples();
		final Set<Triple> testTriples = testData.object1;
		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		final Node s = NodeFactory.createLiteral("s1");
		final Node p = NodeFactory.createURI(RDF.type.getURI());
		final Node o = NodeFactory.createLiteral("o1");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp);
		
		/* Test Union
		assertTrue(translation instanceof SPARQLUnionPattern);
		final Set<org.apache.jena.graph.Triple> translatedTriples = new HashSet<>();
		for(final SPARQLGraphPattern i : ((SPARQLUnionPattern) translation).getSubPatterns()) {
			assert(i instanceof TriplePattern);
			translatedTriples.add(((TriplePattern) i).asJenaTriple());
		}
		*/
		
		//Test Intersection
		assertTrue(translation instanceof BGPImpl);
		final Set<org.apache.jena.graph.Triple> translatedTriples = new HashSet<>();
		for(final TriplePattern i : ((BGPImpl)translation).getTriplePatterns()) {
			translatedTriples.add(i.asJenaTriple());
		}
		
		assertEquals(testData.object2, translatedTriples);
	}
	
	public Pair<Set<Triple>,Set<org.apache.jena.graph.Triple>> CreateTestTriples(){
		final Set<Triple> testSet = new HashSet<>();
		
		Node s = NodeFactory.createLiteral("s1");
		Node p = OWL.sameAs.asNode();
		final Node sRes = NodeFactory.createLiteral("s2");
		testSet.add(new TripleImpl(s, p, sRes));
		
		s = RDF.type.asNode();
		p = OWL.inverseOf.asNode();
		final Node pRes = NodeFactory.createLiteral("Not type");
		testSet.add(new TripleImpl(s, p, pRes));
		
		s = NodeFactory.createLiteral("o1");
		p = OWL.equivalentClass.asNode();
		final Node o = NodeFactory.createBlankNode();
		testSet.add(new TripleImpl(s, p, o));
		
		s = o;
		//p = OWL.unionOf.asNode();
		p = OWL.intersectionOf.asNode();
		final Node o1Res = NodeFactory.createLiteral("o2");
		testSet.add(new TripleImpl(s, p, o1Res));
		
		final Node o2Res = NodeFactory.createLiteral("o3");
		testSet.add(new TripleImpl(s, p, o2Res));
		
		final Set<org.apache.jena.graph.Triple> expectedRes = new HashSet<>();
		expectedRes.add(new org.apache.jena.graph.Triple(o2Res, pRes, sRes));
		expectedRes.add(new org.apache.jena.graph.Triple(o1Res, pRes, sRes));
		
		final Pair<Set<Triple>,Set<org.apache.jena.graph.Triple>> returnP = new Pair<Set<Triple>,Set<org.apache.jena.graph.Triple>>(testSet, expectedRes);
		return returnP;
	}
	
	
}
