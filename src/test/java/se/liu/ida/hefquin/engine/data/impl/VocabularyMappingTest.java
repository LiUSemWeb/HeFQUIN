package se.liu.ida.hefquin.engine.data.impl;

import static org.junit.Assert.assertEquals;

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

public class VocabularyMappingTest {

	@Test
	public void VocabularyMappingConstructorTest() {
		final Set<Triple> testTriples = CreateTestTriples();
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
		final Set<Triple> testTriples = CreateTestTriples();
		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		final Node s = NodeFactory.createLiteral("s1");
		final Node p = NodeFactory.createURI(RDF.type.getURI());
		final Node o = NodeFactory.createLiteral("o1");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp);
		List<SPARQLGraphPattern> subPatterns = new ArrayList<SPARQLGraphPattern>();
		for(SPARQLGraphPattern gp : ((SPARQLUnionPattern)translation).getSubPatterns()) {
			subPatterns.add(gp);
		}
		
		
		final Node sTranslated = NodeFactory.createLiteral("s2");
		final Node pTranslated = NodeFactory.createLiteral("Not type");
		final Node oTranslated1 = NodeFactory.createLiteral("o2");
		final Node oTranslated2 = NodeFactory.createLiteral("o3");
		final TriplePattern tp1 = new TriplePatternImpl(oTranslated1, pTranslated, sTranslated);
		final TriplePattern tp2 = new TriplePatternImpl(oTranslated2, pTranslated, sTranslated);	
		List<SPARQLGraphPattern> union = new ArrayList<SPARQLGraphPattern>();
		union.add(tp1);
		union.add(tp2);
		final SPARQLUnionPattern correct = new SPARQLUnionPatternImpl(union);
		
		//System.out.print(subPatterns.toString() + "\n");
		//System.out.print(union.toString() + "\n");

		assertEquals(union, subPatterns);
	}
	
	public Set<Triple> CreateTestTriples(){
		final Set<Triple> testSet = new HashSet<>();
		
		Node s = NodeFactory.createLiteral("s1");
		Node p = OWL.sameAs.asNode();
		Node o = NodeFactory.createLiteral("s2");
		Triple t = new TripleImpl(s, p, o);
		testSet.add(t);
		
		s = RDF.type.asNode();
		p = OWL.inverseOf.asNode();
		o = NodeFactory.createLiteral("Not type");
		t = new TripleImpl(s, p, o);
		testSet.add(t);
		
		s = NodeFactory.createLiteral("o1");
		p = OWL.equivalentClass.asNode();
		o = NodeFactory.createBlankNode();
		t = new TripleImpl(s, p, o);
		testSet.add(t);
		
		s = o;
		p = OWL.unionOf.asNode();
		o = NodeFactory.createLiteral("o2");
		t = new TripleImpl(s, p, o);
		testSet.add(t);
		
		o = NodeFactory.createLiteral("o3");
		t = new TripleImpl(s, p, o);
		testSet.add(t);
		
		return testSet;
	}
}
