package se.liu.ida.hefquin.engine.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
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
		
		final Node s = NodeFactory.createLiteral("s1");
		final Node p = RDF.type.asNode();
		final Node o = NodeFactory.createLiteral("o1");
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
		Node s = NodeFactory.createLiteral("s1");
		Node p = OWL.sameAs.asNode();
		final Node o1 = NodeFactory.createLiteral("s2");
		testTriples.add(new Triple(s, p, o1));
				
		//Multiple mappings for same subject
		final Node o2 = NodeFactory.createLiteral("s3");
		testTriples.add(new Triple(s, p, o2));
		
		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		p = NodeFactory.createLiteral("p");
		final Node o = NodeFactory.createLiteral("o");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp); 
		final Set<Triple> translationTriples = new HashSet<>();
		assertTrue(translation instanceof SPARQLUnionPatternImpl);
		for (final SPARQLGraphPattern i : ((SPARQLUnionPatternImpl) translation).getSubPatterns()) {
			assertTrue(i instanceof TriplePattern);
			translationTriples.add(((TriplePattern) i).asJenaTriple());
		}
			
		final Set<Triple> expectedResults = new HashSet<>();
		expectedResults.add(new Triple(o1, p, o));
		expectedResults.add(new Triple(o2, p, o));
		
		assertEquals(expectedResults, translationTriples);
	}
	
	@Test
	public void TranslatePredicateTest() {
		final Set<Triple> testTriples = new HashSet<>();
		//Equality
		Node s = NodeFactory.createLiteral("p1");
		Node p = OWL.equivalentProperty.asNode();
		final Node t1 = NodeFactory.createLiteral("p2");
		testTriples.add(new Triple(s, p, t1));
				
		//Predicate inverse
		p = OWL.inverseOf.asNode();
		final Node t2 = NodeFactory.createLiteral("Not p1");
		testTriples.add(new Triple(s, p, t2));
			
		//Predicate subProperty
		final Node t3 = NodeFactory.createLiteral("Subtype");
		p = RDFS.subPropertyOf.asNode();
		testTriples.add(new Triple(t3, p, s));
		
		//Predicate Intersection
		p = OWL.equivalentProperty.asNode();
		final Node t4 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t4));	
		p = OWL.intersectionOf.asNode();
		final Node t5 = NodeFactory.createLiteral("p3");
		testTriples.add(new Triple(t4, p, t5));		
		final Node t6 = NodeFactory.createLiteral("p4");
		testTriples.add(new Triple(t4, p, t6));
		
		//Predicate Union
		p = OWL.equivalentProperty.asNode();
		final Node t7 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t7));	
		p = OWL.unionOf.asNode();
		final Node t8 = NodeFactory.createLiteral("p5");
		testTriples.add(new Triple(t7, p, t8));		
		final Node t9 = NodeFactory.createLiteral("p6");
		testTriples.add(new Triple(t7, p, t9));

		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		s = NodeFactory.createLiteral("s");
		p = NodeFactory.createLiteral("p1");
		final Node o = NodeFactory.createLiteral("o");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp); 

		final Set<Triple> translationTriples = new HashSet<>();
		assertTrue(translation instanceof SPARQLUnionPatternImpl);
		boolean oneUnion = false;
		boolean oneIntersection = false;
		for (final SPARQLGraphPattern i : ((SPARQLUnionPatternImpl) translation).getSubPatterns()) {
			if(i instanceof SPARQLUnionPatternImpl) {
				if(oneUnion) {
					oneUnion = false;
					break;
				} else {
					oneUnion = true;
					for (final SPARQLGraphPattern j : ((SPARQLUnionPatternImpl) i).getSubPatterns()) {
						assertTrue(j instanceof TriplePattern);
						translationTriples.add(((TriplePattern) j).asJenaTriple());
					}
				}
			} else if (i instanceof BGPImpl) {
				if(oneIntersection) {
					oneIntersection = false;
					break;
				} else {
					oneIntersection = true;
					for (final TriplePattern k : ((BGPImpl) i).getTriplePatterns()) {
						translationTriples.add(k.asJenaTriple());
					}
				}
			} else if (i instanceof TriplePattern) {
				translationTriples.add(((TriplePattern) i).asJenaTriple());
			} else {
				throw new IllegalArgumentException(i.getClass().toString());
			}
		}
		assertTrue(oneUnion);
		assertTrue(oneIntersection);
			
		final Set<Triple> expectedResults = new HashSet<>();
		expectedResults.add(new Triple(s, t1, o));
		expectedResults.add(new Triple(o, t2, s));
		expectedResults.add(new Triple(s, t3, o));
		expectedResults.add(new Triple(s, t5, o));
		expectedResults.add(new Triple(s, t6, o));
		expectedResults.add(new Triple(s, t8, o));
		expectedResults.add(new Triple(s, t9, o));

		/*
		final SPARQLUnionPatternImpl expectedResults = new SPARQLUnionPatternImpl();
		expectedResults.addSubPattern(new TriplePatternImpl(s, t1, o));
		expectedResults.addSubPattern(new TriplePatternImpl(o, t2, s));
		expectedResults.addSubPattern(new TriplePatternImpl(s, t3, o));
		final BGPImpl intersection = new BGPImpl();
		intersection.addTriplePattern(new TriplePatternImpl(s, t5, o));
		intersection.addTriplePattern(new TriplePatternImpl(s, t6, o));
		expectedResults.addSubPattern(intersection);
		final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
		union.addSubPattern(new TriplePatternImpl(s, t8, o));
		union.addSubPattern(new TriplePatternImpl(s, t9, o));
		expectedResults.addSubPattern(union);
		
		assertEquals(expectedResults, translation);
		*/
		
		assertEquals(expectedResults, translationTriples);
	}
	
	@Test
	public void TranslateObjectTest() {
		final Set<Triple> testTriples = new HashSet<>();
		//Equality
		Node s = NodeFactory.createLiteral("o1");
		Node p = OWL.equivalentClass.asNode();
		final Node t1 = NodeFactory.createLiteral("o2");
		testTriples.add(new Triple(s, p, t1));
		
		//Equality
		p = OWL.sameAs.asNode();
		final Node t2 = NodeFactory.createLiteral("o3");
		testTriples.add(new Triple(s, p, t2));	
		
		//Object subClass
		final Node t3 = NodeFactory.createLiteral("Subclass");
		p = RDFS.subClassOf.asNode();
		testTriples.add(new Triple(t3, p, s));
		
		//Object Intersection
		p = OWL.equivalentClass.asNode();
		final Node t4 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t4));	
		p = OWL.intersectionOf.asNode();
		final Node t5 = NodeFactory.createLiteral("o4");
		testTriples.add(new Triple(t4, p, t5));		
		final Node t6 = NodeFactory.createLiteral("o5");
		testTriples.add(new Triple(t4, p, t6));
		
		//Object Union
		p = OWL.equivalentClass.asNode();
		final Node t7 = NodeFactory.createBlankNode();
		testTriples.add(new Triple(s, p, t7));	
		p = OWL.unionOf.asNode();
		final Node t8 = NodeFactory.createLiteral("o6");
		testTriples.add(new Triple(t7, p, t8));		
		final Node t9 = NodeFactory.createLiteral("o7");
		testTriples.add(new Triple(t7, p, t9));

		final VocabularyMapping vm = new VocabularyMappingImpl(testTriples);
		
		s = NodeFactory.createLiteral("s");
		p = RDF.type.asNode();
		final Node o = NodeFactory.createLiteral("o1");
		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern translation = vm.translateTriplePattern(testTp); 
		final Set<Triple> translationTriples = new HashSet<>();
		assertTrue(translation instanceof SPARQLUnionPatternImpl);
		boolean oneUnion = false;
		boolean oneIntersection = false;
		for (final SPARQLGraphPattern i : ((SPARQLUnionPatternImpl) translation).getSubPatterns()) {
			if(i instanceof SPARQLUnionPatternImpl) {
				if(oneUnion) {
					oneUnion = false;
					break;
				} else {
					oneUnion = true;
					for (final SPARQLGraphPattern j : ((SPARQLUnionPatternImpl) i).getSubPatterns()) {
						assertTrue(j instanceof TriplePattern);
						translationTriples.add(((TriplePattern) j).asJenaTriple());
					}
				}
			} else if (i instanceof BGPImpl) {
				if(oneIntersection) {
					oneIntersection = false;
					break;
				} else {
					oneIntersection = true;
					for (final TriplePattern k : ((BGPImpl) i).getTriplePatterns()) {
						translationTriples.add(k.asJenaTriple());
					}
				}
			} else if (i instanceof TriplePattern) {
				translationTriples.add(((TriplePattern) i).asJenaTriple());
			} else {
				throw new IllegalArgumentException(i.getClass().toString());
			}
		}
		System.out.print("\n");
		assertTrue(oneUnion);
		assertTrue(oneIntersection);
			
		final Set<Triple> expectedResults = new HashSet<>();
		expectedResults.add(new Triple(s, p, t1));
		expectedResults.add(new Triple(s, p, t2));
		expectedResults.add(new Triple(s, p, t3));
		expectedResults.add(new Triple(s, p, t5));
		expectedResults.add(new Triple(s, p, t6));
		expectedResults.add(new Triple(s, p, t8));
		expectedResults.add(new Triple(s, p, t9));
		
		assertEquals(expectedResults, translationTriples);
	}
	
	public Pair<Set<Triple>, Set<Triple>> CreateTestTriples(){
		final Set<Triple> testSet = new HashSet<>();
		
		//Equality
		Node s = NodeFactory.createLiteral("s1");
		Node p = OWL.sameAs.asNode();
		final Node s1Res = NodeFactory.createLiteral("s2");
		testSet.add(new Triple(s, p, s1Res));
		
		//Multiple mappings for same subject
		final Node s2Res = NodeFactory.createLiteral("s3");
		testSet.add(new Triple(s, p, s2Res));
		
		//Predicate inverse
		s = RDF.type.asNode();
		p = OWL.inverseOf.asNode();
		final Node p1Res = NodeFactory.createLiteral("Not type");
		testSet.add(new Triple(s, p, p1Res));
			
		//Predicate subProperty
		Node o = s;
		final Node p2Res = NodeFactory.createLiteral("Subtype");
		p = RDFS.subPropertyOf.asNode();
		testSet.add(new Triple(p2Res, p, o));
		
		//Object Intersection or union
		s = NodeFactory.createLiteral("o1");
		p = OWL.equivalentClass.asNode();
		o = NodeFactory.createBlankNode();
		testSet.add(new Triple(s, p, o));
		
		s = o;
		//p = OWL.unionOf.asNode();
		p = OWL.intersectionOf.asNode();
		final Node o1Res = NodeFactory.createLiteral("o2");
		testSet.add(new Triple(s, p, o1Res));
		
		final Node o2Res = NodeFactory.createLiteral("o3");
		testSet.add(new Triple(s, p, o2Res));
		
		/* Wrong predicate should lead to error
		p = OWL.unionOf.asNode();
		o = NodeFactory.createLiteral("o4");
		testSet.add(new Triple(s, p, o));
		*/
		
		 /*Not possible mapping should lead to error
		 p = OWL.inverseOf.asNode();
		 o = NodeFactory.createLiteral("o5");
		 testSet.add(new Triple(s, p, o));
		 */
		 
		
		/*
		SPARQLUnionPatternImpl pUnion1 = new SPARQLUnionPatternImpl();
		pUnion1.addSubPattern(new TriplePatternImpl(s1Res, p2Res, o1Res));
		pUnion1.addSubPattern(new TriplePatternImpl(o1Res, p1Res, s1Res));
		
		SPARQLUnionPatternImpl pUnion2 = new SPARQLUnionPatternImpl();
		pUnion2.addSubPattern(new TriplePatternImpl(s1Res, p2Res, o2Res));
		pUnion2.addSubPattern(new TriplePatternImpl(o2Res, p1Res, s1Res));
		
		SPARQLUnionPatternImpl pUnion3 = new SPARQLUnionPatternImpl();
		pUnion3.addSubPattern(new TriplePatternImpl(s2Res, p2Res, o1Res));
		pUnion3.addSubPattern(new TriplePatternImpl(o1Res, p1Res, s2Res));
		
		SPARQLUnionPatternImpl pUnion4 = new SPARQLUnionPatternImpl();
		pUnion4.addSubPattern(new TriplePatternImpl(s2Res, p2Res, o2Res));
		pUnion4.addSubPattern(new TriplePatternImpl(o2Res, p1Res, s2Res));
		
		List<SPARQLGraphPattern> oList1 = new ArrayList<>();
		oList1.add(pUnion1);
		oList1.add(pUnion2);
		SPARQLGroupPattern oIntersection1 = new SPARQLGroupPatternImpl(oList1);
		
		List<SPARQLGraphPattern> oList2 = new ArrayList<>();
		oList2.add(pUnion3);
		oList2.add(pUnion4);
		SPARQLGroupPattern oIntersection2 = new SPARQLGroupPatternImpl(oList2);
		
		SPARQLUnionPatternImpl sUnion = new SPARQLUnionPatternImpl();
		sUnion.addSubPattern(oIntersection1);
		sUnion.addSubPattern(oIntersection2);
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
