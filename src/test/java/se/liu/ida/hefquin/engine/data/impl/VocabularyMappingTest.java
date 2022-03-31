package se.liu.ida.hefquin.engine.data.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.BindingBuilder;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
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
	public void TranslateSolutionMappingTest() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
				+ "@prefix ex:   <http://example.org/> .  \n"
				+ "ex:a owl:equivalentClass ex:n . "
				+ "ex:b owl:unionOf (ex:c ex:n) . "
				+ "ex:d rdfs:subClassOf ex:n . "
				+ "ex:e owl:equivalentProperty ex:m . "
				+ "ex:f rdfs:subPropertyOf ex:m . "
				+ "ex:g owl:sameAs ex:o . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);
		final Set<Triple> mappingSet = new HashSet<>( RiotLib.triples(mapping, Node.ANY, Node.ANY, Node.ANY) );
		
		final VocabularyMapping vm = new VocabularyMappingImpl(mappingSet);
		
		final BindingBuilder testBuilder = BindingBuilder.create();
		testBuilder.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/n"));
		testBuilder.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/m"));
		testBuilder.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/o"));
		
		final SolutionMapping testSm = new SolutionMappingImpl(testBuilder.build());		
		Set<SolutionMapping> translation = vm.translateSolutionMapping(testSm);
		
		Set<SolutionMapping> expectedResults = new HashSet<>();
		final BindingBuilder first = BindingBuilder.create();
		first.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/a"));
		first.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/e"));
		first.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/g"));
		expectedResults.add(new SolutionMappingImpl(first.build()));
		
		final BindingBuilder second = BindingBuilder.create();
		second.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/b"));
		second.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/e"));
		second.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/g"));
		expectedResults.add(new SolutionMappingImpl(second.build()));
		
		final BindingBuilder third = BindingBuilder.create();
		third.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/d"));
		third.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/e"));
		third.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/g"));
		expectedResults.add(new SolutionMappingImpl(third.build()));
		
		final BindingBuilder fourth = BindingBuilder.create();
		fourth.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/a"));
		fourth.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/f"));
		fourth.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/g"));
		expectedResults.add(new SolutionMappingImpl(fourth.build()));
		
		final BindingBuilder fifth = BindingBuilder.create();
		fifth.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/b"));
		fifth.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/f"));
		fifth.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/g"));
		expectedResults.add(new SolutionMappingImpl(fifth.build()));
		
		final BindingBuilder sixth = BindingBuilder.create();
		sixth.add(Var.alloc("v"), NodeFactory.createURI("http://example.org/d"));
		sixth.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/f"));
		sixth.add(Var.alloc("x"), NodeFactory.createURI("http://example.org/g"));
		expectedResults.add(new SolutionMappingImpl(sixth.build()));

		assertEquals(expectedResults, translation);
		
	}
	
	//Test the two cases in which a binding is not translated
	@Test
	public void TranslateSolutionMappingTestNoTranslation() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
				+ "@prefix ex:   <http://example.org/> .  \n"
				+ "ex:g owl:sameAs ex:o . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);
		final Set<Triple> mappingSet = new HashSet<>( RiotLib.triples(mapping, Node.ANY, Node.ANY, Node.ANY) );
		
		final VocabularyMapping vm = new VocabularyMappingImpl(mappingSet);
		
		final BindingBuilder testBuilder = BindingBuilder.create();
		testBuilder.add(Var.alloc("v"), RDF.type.asNode());
		testBuilder.add(Var.alloc("w"), NodeFactory.createURI("http://example.org/m"));
		
		final SolutionMapping testSm = new SolutionMappingImpl(testBuilder.build());		
		Set<SolutionMapping> translation = vm.translateSolutionMapping(testSm);
		
		Set<SolutionMapping> expectedResults = new HashSet<>();
		expectedResults.add(testSm);

		assertEquals(expectedResults, translation);
		
	}

}
