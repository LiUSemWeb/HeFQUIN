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
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.engine.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.BGPImpl;
import se.liu.ida.hefquin.engine.query.impl.SPARQLGroupPatternImpl;
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
