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
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;

public class VocabularyMappingTest
{
	@Test
	public void VocabularyMappingConstructorTest() {
		final Pair<Set<Triple>,Set<Triple>> testData = CreateTestTriples();

		final VocabularyMappingImpl vm = new VocabularyMappingImpl(testData.object1);

		final Set<Triple> queryResults = new HashSet<>();
		final Iterator<Triple> i = vm.getVocabularyMappingAsGraph().find();
		while(i.hasNext()) {
			queryResults.add(i.next());
		}
		
		assertEquals(queryResults, testData.object1);
	}
	
	@Test
	public void TranslateTriplePatternTest() {
		final Pair<Set<Triple>,Set<Triple>> testData = CreateTestTriples();

		final VocabularyMapping vm = new VocabularyMappingImpl(testData.object1);
		
		final Node s = NodeFactory.createURI("s1");
		final Node p = RDF.type.asNode();
		final Node o = NodeFactory.createURI("o1");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp);
		
		final Set<Triple> translationTriples = new HashSet<>();
		assertTrue(translation instanceof SPARQLUnionPatternImpl);
		for (final SPARQLGraphPattern i : ((SPARQLUnionPatternImpl) translation).getSubPatterns()) {
			assertTrue(i instanceof SPARQLGroupPattern);
			for (final SPARQLGraphPattern j : ((SPARQLGroupPattern) i).getSubPatterns()) {
				assertTrue(j instanceof SPARQLUnionPatternImpl);
				for (final SPARQLGraphPattern k : ((SPARQLUnionPatternImpl) j).getSubPatterns()) {
					assertTrue(k instanceof TriplePattern);
					translationTriples.add(((TriplePattern) k).asJenaTriple());
				}
			}
		}
		
		assertEquals(testData.object2, translationTriples);
	}
	
	@Test
	public void TranslateSubjectTest() {
		final Set<Triple> testTriples = new HashSet<>();
		//Equality
		Node s = NodeFactory.createURI("s1");
		Node p = OWL.sameAs.asNode();
		final Node t1 = NodeFactory.createURI("s2");
		testTriples.add(new Triple(s, p, t1));
				
		//Multiple mappings for same subject
		final Node t2 = NodeFactory.createURI("s3");
		testTriples.add(new Triple(s, p, t2));
		
		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		p = NodeFactory.createURI("p");
		final Node o = NodeFactory.createURI("o");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp);
		List<SPARQLGraphPattern> translationSubPatterns = new ArrayList<>();
		assertTrue(translation instanceof SPARQLUnionPattern);
		for(SPARQLGraphPattern i : ((SPARQLUnionPattern) translation).getSubPatterns()) {
			translationSubPatterns.add(i);
		}
		
		List<SPARQLGraphPattern> expectedResults = new ArrayList<>();
		expectedResults.add(new TriplePatternImpl(t1, p, o));
		expectedResults.add(new TriplePatternImpl(t2, p, o));
		
		assertTrue(translationSubPatterns.containsAll(expectedResults));
		assertTrue(translationSubPatterns.size() == expectedResults.size());
	}
	
	@Test
	public void TranslatePredicateTest() {
		final Set<Triple> testTriples = new HashSet<>();
		//Equality
		Node s = NodeFactory.createURI("p1");
		Node p = OWL.equivalentProperty.asNode();
		final Node t1 = NodeFactory.createURI("p2");
		testTriples.add(new Triple(s, p, t1));
				
		//Predicate inverse
		p = OWL.inverseOf.asNode();
		final Node t2 = NodeFactory.createURI("Not p1");
		testTriples.add(new Triple(s, p, t2));
			
		//Predicate subProperty
		final Node t3 = NodeFactory.createURI("Subtype");
		p = RDFS.subPropertyOf.asNode();
		testTriples.add(new Triple(t3, p, s));
		
		//Predicate Intersection
		p = OWL.intersectionOf.asNode();
		final Node t4 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t4));	
		
		p = RDF.first.asNode();
		final Node t5 = NodeFactory.createURI("p3");
		testTriples.add(new Triple(t4, p, t5));	
		
		p = RDF.rest.asNode();
		final Node t6 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(t4, p, t6));
		
		p = RDF.first.asNode();
		final Node t7 = NodeFactory.createURI("p4");
		testTriples.add(new Triple(t6, p, t7));	
		
		p = RDF.rest.asNode();
		Node o = RDF.nil.asNode();
		testTriples.add(new Triple(t6, p, o));
		
		//Predicate Union
		p = OWL.unionOf.asNode();
		final Node t8 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t8));	
		
		p = RDF.first.asNode();
		final Node t9 = NodeFactory.createURI("p5");
		testTriples.add(new Triple(t8, p, t9));	
		
		p = RDF.rest.asNode();
		final Node t10 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(t8, p, t10));
		
		p = RDF.first.asNode();
		final Node t11 = NodeFactory.createURI("p6");
		testTriples.add(new Triple(t10, p, t11));	
		
		p = RDF.rest.asNode();
		testTriples.add(new Triple(t10, p, o));
		

		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		s = NodeFactory.createURI("s");
		p = NodeFactory.createURI("p1");
		o = NodeFactory.createURI("o");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp); 
		List<SPARQLGraphPattern> translationSubPatterns = new ArrayList<>();
		assertTrue(translation instanceof SPARQLUnionPattern);
		for(SPARQLGraphPattern i : ((SPARQLUnionPattern) translation).getSubPatterns()) {
			if(i instanceof SPARQLUnionPattern) {
				assertTrue(((SPARQLUnionPattern) i).getNumberOfSubPatterns() == 2);
				for(SPARQLGraphPattern j : ((SPARQLUnionPattern) i).getSubPatterns()) {
					translationSubPatterns.add(j);
				}
			} else {
				translationSubPatterns.add(i);
			}
		}
	
		final List<SPARQLGraphPattern> expectedResults = new ArrayList<>();
		expectedResults.add(new TriplePatternImpl(s, t3, o));
		expectedResults.add(new TriplePatternImpl(s, t1, o));
		
		//Union subpatterns
		expectedResults.add(new TriplePatternImpl(s, t9, o));
		expectedResults.add(new TriplePatternImpl(s, t11, o));
		
		final BGPImpl intersection = new BGPImpl();
		intersection.addTriplePattern(new TriplePatternImpl(s, t5, o));
		intersection.addTriplePattern(new TriplePatternImpl(s, t7, o));
		expectedResults.add(intersection);
		
		expectedResults.add(new TriplePatternImpl(o, t2, s));
		
		assertTrue(translationSubPatterns.containsAll(expectedResults));
		assertTrue(translationSubPatterns.size() == expectedResults.size());
	}
	
	@Test
	public void TranslateObjectTest() {
		final Set<Triple> testTriples = new HashSet<>();
		//Equality
		Node s = NodeFactory.createURI("o1");
		Node p = OWL.equivalentClass.asNode();
		final Node t1 = NodeFactory.createURI("o2");
		testTriples.add(new Triple(s, p, t1));
		
		//Equality
		p = OWL.sameAs.asNode();
		final Node t2 = NodeFactory.createURI("o3");
		testTriples.add(new Triple(s, p, t2));	
		
		//Object subClass
		final Node t3 = NodeFactory.createURI("Subclass");
		p = RDFS.subClassOf.asNode();
		testTriples.add(new Triple(t3, p, s));
		
		
		//Object Intersection
		p = OWL.intersectionOf.asNode();
		final Node t4 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t4));	
		
		p = RDF.first.asNode();
		final Node t5 = NodeFactory.createURI("o4");
		testTriples.add(new Triple(t4, p, t5));	
		
		p = RDF.rest.asNode();
		final Node t6 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(t4, p, t6));
		
		p = RDF.first.asNode();
		final Node t7 = NodeFactory.createURI("o5");
		testTriples.add(new Triple(t6, p, t7));	
		
		p = RDF.rest.asNode();
		Node o = RDF.nil.asNode();
		testTriples.add(new Triple(t6, p, o));
		
		//Object Union
		p = OWL.unionOf.asNode();
		final Node t8 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t8));	
		
		p = RDF.first.asNode();
		final Node t9 = NodeFactory.createURI("o6");
		testTriples.add(new Triple(t8, p, t9));	
		
		p = RDF.rest.asNode();
		final Node t10 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(t8, p, t10));
		
		p = RDF.first.asNode();
		final Node t11 = NodeFactory.createURI("o7");
		testTriples.add(new Triple(t10, p, t11));	
		
		p = RDF.rest.asNode();
		testTriples.add(new Triple(t10, p, o));

		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		s = NodeFactory.createURI("s");
		p = RDF.type.asNode();
		o = NodeFactory.createURI("o1");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp); 
		List<SPARQLGraphPattern> translationSubPatterns = new ArrayList<>();
		assertTrue(translation instanceof SPARQLUnionPattern);
		for(SPARQLGraphPattern i : ((SPARQLUnionPattern) translation).getSubPatterns()) {
			if(i instanceof SPARQLUnionPattern) {
				assertTrue(((SPARQLUnionPattern) i).getNumberOfSubPatterns() == 2);
				for(SPARQLGraphPattern j : ((SPARQLUnionPattern) i).getSubPatterns()) {
					translationSubPatterns.add(j);
				}
			} else {
				translationSubPatterns.add(i);
			}
		}
			
		List<SPARQLGraphPattern> expectedResults = new ArrayList<>();
		expectedResults.add(new TriplePatternImpl(s, p, t1));
		expectedResults.add(new TriplePatternImpl(s, p, t2));
		expectedResults.add(new TriplePatternImpl(s, p, t3));
		
		//Union subpatterns
		expectedResults.add(new TriplePatternImpl(s, p, t9));
		expectedResults.add(new TriplePatternImpl(s, p, t11));
		
		final BGPImpl intersection = new BGPImpl();
		intersection.addTriplePattern(new TriplePatternImpl(s, p, t5));
		intersection.addTriplePattern(new TriplePatternImpl(s, p, t7));
		expectedResults.add(intersection);
		
		assertTrue(translationSubPatterns.containsAll(expectedResults));
		assertTrue(translationSubPatterns.size() == expectedResults.size());
	}
	
	public Pair<Set<Triple>, Set<Triple>> CreateTestTriples(){
		final Set<Triple> testSet = new HashSet<>();
		
		//Equality
		Node s = NodeFactory.createURI("s1");
		Node p = OWL.sameAs.asNode();
		final Node s1Res = NodeFactory.createURI("s2");
		testSet.add(new Triple(s, p, s1Res));
		
		//Multiple mappings for same subject
		final Node s2Res = NodeFactory.createURI("s3");
		testSet.add(new Triple(s, p, s2Res));
		
		//Predicate inverse
		s = RDF.type.asNode();
		p = OWL.inverseOf.asNode();
		final Node p1Res = NodeFactory.createURI("Not type");
		testSet.add(new Triple(s, p, p1Res));
			
		//Predicate subProperty
		Node o = s;
		final Node p2Res = NodeFactory.createURI("Subtype");
		p = RDFS.subPropertyOf.asNode();
		testSet.add(new Triple(p2Res, p, o));
		
		//Object Intersection or union
		s = NodeFactory.createURI("o1");
		//p = OWL.unionOf.asNode();
		p = OWL.intersectionOf.asNode();
		o = NodeFactory.createBlankNode();
		testSet.add(new Triple(s, p, o));
		
		s = o;
		p = RDF.first.asNode();
		final Node o1Res = NodeFactory.createURI("o2");
		testSet.add(new Triple(s, p, o1Res));
		
		p = RDF.rest.asNode();
		o = NodeFactory.createBlankNode();
		testSet.add(new Triple(s, p, o));
		
		s = o;
		p = RDF.first.asNode();
		final Node o2Res = NodeFactory.createURI("o3");
		testSet.add(new Triple(s, p, o2Res));
		
		p = RDF.rest.asNode();
		o = RDF.nil.asNode();
		testSet.add(new Triple(s, p, o));
		
		
		
		/* Wrong predicate should lead to error
		p = OWL.unionOf.asNode();
		o = NodeFactory.createURI("o4");
		testSet.add(new Triple(s, p, o));
		*/
		
		 /*Not possible mapping should lead to error
		 p = OWL.inverseOf.asNode();
		 o = NodeFactory.createURI("o5");
		 testSet.add(new Triple(s, p, o));
		 */
		
		Set<Triple> expectedResults = new HashSet<>();
		expectedResults.add(new Triple(s1Res, p2Res, o1Res));
		expectedResults.add(new Triple(o1Res, p1Res, s1Res));
		expectedResults.add(new Triple(s1Res, p2Res, o2Res));
		expectedResults.add(new Triple(o2Res, p1Res, s1Res));
		expectedResults.add(new Triple(s2Res, p2Res, o1Res));
		expectedResults.add(new Triple(o1Res, p1Res, s2Res));
		expectedResults.add(new Triple(s2Res, p2Res, o2Res));
		expectedResults.add(new Triple(o2Res, p1Res, s2Res));
		
		return new Pair<>(testSet, expectedResults);
	}

}
