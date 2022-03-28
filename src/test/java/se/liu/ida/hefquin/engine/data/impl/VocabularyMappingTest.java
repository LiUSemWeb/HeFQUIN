package se.liu.ida.hefquin.engine.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.query.BGP;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.utils.Pair;

public class VocabularyMappingTest
{
	/*
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
		
		assertTrue(translation instanceof SPARQLUnionPattern);
		final Set<Triple> translatedTriples = new HashSet<>();
		for(final SPARQLGraphPattern i : ((SPARQLUnionPattern) translation).getSubPatterns()) {
			assert(i instanceof TriplePattern);
			translatedTriples.add(((TriplePattern) i).asJenaTriple());
		}
		
		//Test Intersection
		assertTrue(translation instanceof BGP);
		final Set<Triple> translatedTriples = new HashSet<>();
		for(final TriplePattern i : ((BGP)translation).getTriplePatterns()) {
			translatedTriples.add(i.asJenaTriple());
		}
		
		assertEquals(testData.object2, translatedTriples);
	}
	
	public Pair<Set<Triple>,Set<Triple>> CreateTestTriples(){
		final Set<Triple> testSet = new HashSet<>();
		
		Node s = NodeFactory.createLiteral("s1");
		Node p = OWL.sameAs.asNode();
		final Node sRes = NodeFactory.createLiteral("s2");
		testSet.add(new Triple(s, p, sRes));
		
		s = RDF.type.asNode();
		p = OWL.inverseOf.asNode();
		final Node pRes = NodeFactory.createLiteral("Not type");
		testSet.add(new Triple(s, p, pRes));
		
		s = NodeFactory.createLiteral("o1");
		p = OWL.equivalentClass.asNode();
		final Node o = NodeFactory.createBlankNode();
		testSet.add(new Triple(s, p, o));
		
		s = o;
		//p = OWL.unionOf.asNode();
		p = OWL.intersectionOf.asNode();
		final Node o1Res = NodeFactory.createLiteral("o2");
		testSet.add(new Triple(s, p, o1Res));
		
		final Node o2Res = NodeFactory.createLiteral("o3");
		testSet.add(new Triple(s, p, o2Res));
		
		final Set<Triple> expectedRes = new HashSet<>();
		expectedRes.add(new Triple(o2Res, pRes, sRes));
		expectedRes.add(new Triple(o1Res, pRes, sRes));
		
		return new Pair<>(testSet, expectedRes);
	}
	*/
	
	@Test
	public void TranslateSolutionMappingTest() {
		final Set<Triple> testSet = new HashSet<>();
		Node s = NodeFactory.createURI("a");
		Node p = OWL.equivalentClass.asNode();
		Node o = NodeFactory.createURI("n");
		testSet.add(new Triple(s, p ,o));
		
		s = NodeFactory.createURI("b");
		p = OWL.unionOf.asNode();
		o = NodeFactory.createBlankNode();
		testSet.add(new Triple(s, p ,o));
		
		s = o;
		p = RDF.first.asNode();
		o = NodeFactory.createURI("c");
		testSet.add(new Triple(s, p ,o));
		
		p = RDF.rest.asNode();
		o = NodeFactory.createBlankNode();
		testSet.add(new Triple(s, p ,o));
		
		s = o;
		p = RDF.first.asNode();
		o = NodeFactory.createURI("n");
		testSet.add(new Triple(s, p ,o));
		
		p = RDF.rest.asNode();
		o = RDF.nil.asNode();
		testSet.add(new Triple(s, p ,o));
		
		s = NodeFactory.createURI("c");
		p = OWL.equivalentClass.asNode();
		o = NodeFactory.createURI("m");
		testSet.add(new Triple(s, p ,o));
		
		final VocabularyMapping vm = new VocabularyMappingImpl(testSet);
		
		final BindingBuilder testBuilder = BindingBuilder.create();
		testBuilder.add(Var.alloc("v"), NodeFactory.createURI("n"));
		testBuilder.add(Var.alloc("w"), NodeFactory.createURI("m"));
		
		final SolutionMapping testSm = new SolutionMappingImpl(testBuilder.build());		
		Set<SolutionMapping> translation = vm.translateSolutionMapping(testSm);
		
		Set<SolutionMapping> expectedResults = new HashSet<>();
		final BindingBuilder first = BindingBuilder.create();
		first.add(Var.alloc("v"), NodeFactory.createURI("a"));
		first.add(Var.alloc("w"), NodeFactory.createURI("c"));
		expectedResults.add(new SolutionMappingImpl(first.build()));
		
		final BindingBuilder second = BindingBuilder.create();
		second.add(Var.alloc("v"), NodeFactory.createURI("b"));
		second.add(Var.alloc("w"), NodeFactory.createURI("c"));
		expectedResults.add(new SolutionMappingImpl(second.build()));
		
		System.out.print(expectedResults.toString());
		System.out.print(translation.toString());
		
		//assertTrue(expectedResults.equals(translation));
	}

}
