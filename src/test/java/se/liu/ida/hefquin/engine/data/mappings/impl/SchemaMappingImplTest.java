package se.liu.ida.hefquin.engine.data.mappings.impl;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.system.RiotLib;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;
import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.VocabularyMapping;
import se.liu.ida.hefquin.engine.data.mappings.TermMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SchemaMappingImplTest {

	protected class SchemaMappingImplForTests extends SchemaMappingImpl {

		public SchemaMappingImplForTests(final Graph mappingDescription) {
			super(mappingDescription);
		}

	}

	@Test
	public void parseMappingDescriptionTestEquivalence() throws IOException {
		final String mappingAsTurtle =
				"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
						+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
						+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
						+ "@prefix ex:   <http://example.org/> .  \n"
						+ "ex:n owl:equivalentClass ex:a . "
						+ "ex:n owl:equivalentClass ex:b . "
						+ "ex:m owl:equivalentProperty ex:c . "
						+ "ex:m owl:equivalentProperty ex:d . "
						+ "ex:m owl:equivalentProperty ex:e . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);

		final SchemaMappingImplForTests schemaMapping = new SchemaMappingImplForTests(mapping);

		// Create global nodes
		final Node n = NodeFactory.createURI("http://example.org/n");
		final Node m = NodeFactory.createURI("http://example.org/m");

		// Create local nodes
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node b = NodeFactory.createURI("http://example.org/b");
		final Node c = NodeFactory.createURI("http://example.org/c");
		final Node d = NodeFactory.createURI("http://example.org/d");
		final Node e = NodeFactory.createURI("http://example.org/e");

		assertEquals(2, schemaMapping.g2lMap.keySet().size());
		final Iterator<TermMapping> itG2lN = schemaMapping.g2lMap.get(n).iterator();
		while (itG2lN.hasNext()) {
			TermMapping termMappingN = itG2lN.next();
			if (termMappingN.getTypeOfRule().equals(OWL.equivalentClass.asNode())) {
				assertTrue(termMappingN.getTranslatedTerms().contains(a)
						|| termMappingN.getTranslatedTerms().contains(b));
			}

			final Iterator<TermMapping> itG2lM = schemaMapping.g2lMap.get(m).iterator();
			while (itG2lM.hasNext()) {
				TermMapping termMappingM = itG2lM.next();
				if (termMappingM.getTypeOfRule().equals(OWL.equivalentClass.asNode())) {
					assertTrue(termMappingM.getTranslatedTerms().contains(c)
							|| termMappingM.getTranslatedTerms().contains(d)
							|| termMappingM.getTranslatedTerms().contains(e));
				}
			}

			assertEquals( 5, schemaMapping.l2gMap.keySet().size() );
		}
	}

	@Test
	public void parseMappingDescriptionTestUnionOf() throws IOException {
		final String mappingAsTurtle =
				"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
						+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
						+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
						+ "@prefix ex:   <http://example.org/> .  \n"
						+ "ex:n owl:equivalentClass ex:a . "
						+ "ex:n owl:unionOf (ex:p ex:q) . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);

		final SchemaMappingImplForTests schemaMapping = new SchemaMappingImplForTests(mapping);

		// Create global nodes
		final Node n = NodeFactory.createURI("http://example.org/n");

		// Create local nodes
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node q = NodeFactory.createURI("http://example.org/q");

		assertEquals(1, schemaMapping.g2lMap.keySet().size());
		final Iterator<TermMapping> itG2lN = schemaMapping.g2lMap.get(n).iterator();
		while (itG2lN.hasNext()) {
			TermMapping termMappingN = itG2lN.next();
			if (termMappingN.getTypeOfRule().equals(OWL.equivalentClass.asNode())) {
				assertTrue(termMappingN.getTranslatedTerms().contains(a));
			}
			else if (termMappingN.getTypeOfRule().equals(OWL.unionOf.asNode())) {
				assertTrue(termMappingN.getTranslatedTerms().contains(p)
				&& termMappingN.getTranslatedTerms().contains(q));
			}
		}

		assertEquals( 3, schemaMapping.l2gMap.keySet().size() );
		final Iterator<TermMapping> itL2gP = schemaMapping.l2gMap.get(p).iterator();
		while (itL2gP.hasNext()) {
			TermMapping termMappingP = itL2gP.next();
			if (termMappingP.getTypeOfRule().equals(OWL.unionOf.asNode())) {
				assertTrue(termMappingP.getTranslatedTerms().contains(n));
			}
		}
	}

	@Test
	public void parseMappingDescriptionTestComplete() throws IOException {
		final String mappingAsTurtle =
				"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
						+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
						+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
						+ "@prefix ex:   <http://example.org/> .  \n"
						+ "ex:n owl:equivalentClass ex:a . "
						+ "ex:o owl:equivalentClass ex:p . "
						+ "ex:m owl:equivalentProperty ex:c . "
						+ "ex:m owl:equivalentProperty ex:d . "
						+ "ex:m owl:equivalentProperty ex:e . "
						+ "ex:f rdfs:subPropertyOf ex:m . "
						+ "ex:g rdfs:subPropertyOf ex:m . "
						+ "ex:p rdfs:subClassOf ex:n . "
						+ "ex:n owl:unionOf (ex:p ex:q) . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);

		final SchemaMappingImplForTests schemaMapping = new SchemaMappingImplForTests(mapping);

		// Create global nodes
		final Node n = NodeFactory.createURI("http://example.org/n");
		final Node m = NodeFactory.createURI("http://example.org/m");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Node f = NodeFactory.createURI("http://example.org/f");
		final Node g = NodeFactory.createURI("http://example.org/g");

		// Create local nodes
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node p = NodeFactory.createURI("http://example.org/p");

		assertEquals(3, schemaMapping.g2lMap.keySet().size());
		final Iterator<TermMapping> itG2lN = schemaMapping.g2lMap.get(n).iterator();
		while (itG2lN.hasNext()) {
			TermMapping termMappingN = itG2lN.next();
			if (termMappingN.getTypeOfRule().equals(OWL.equivalentClass.asNode())) {
				assertTrue(termMappingN.getTranslatedTerms().contains(a));
			}
			else if (termMappingN.getTypeOfRule().equals(RDFS.subClassOf.asNode())) {
				assertTrue(termMappingN.getTranslatedTerms().contains(p));
			}

			final Iterator<TermMapping> itG2lM = schemaMapping.g2lMap.get(m).iterator();
			while (itG2lM.hasNext()) {
				TermMapping termMappingM = itG2lM.next();
				if (termMappingM.getTypeOfRule().equals(RDFS.subPropertyOf.asNode())) {
					assertTrue(termMappingM.getTranslatedTerms().contains(f)
							|| termMappingM.getTranslatedTerms().contains(g));
				}
			}
		}

		assertEquals( 8, schemaMapping.l2gMap.keySet().size() );
		final Iterator<TermMapping> itL2gP = schemaMapping.l2gMap.get(p).iterator();
		while (itL2gP.hasNext()) {
			TermMapping termMappingP = itL2gP.next();
			if (termMappingP.getTypeOfRule().equals(OWL.equivalentClass.asNode())) {
				assertTrue(termMappingP.getTranslatedTerms().contains(o));
			}
			else if (termMappingP.getTypeOfRule().equals(RDFS.subClassOf.asNode())) {
				assertTrue(termMappingP.getTranslatedTerms().contains(n));
			}
			else if (termMappingP.getTypeOfRule().equals(OWL.unionOf.asNode())) {
				assertTrue(termMappingP.getTranslatedTerms().contains(n));
			}
		}
	}

	@Test
	public void TranslateTriplePatternTest() throws IOException {
		final String mappingAsTurtle =
				"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
						+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
						+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
						+ "@prefix ex:   <http://example.org/> .  \n"
						+ "ex:o1 owl:equivalentClass ex:o4 . "
						+ "ex:subType rdfs:subPropertyOf rdf:type . "
						+ "ex:o1 owl:unionOf (ex:o2 ex:o3) . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);
		final Set<Triple> mappingSet = new HashSet<>( RiotLib.triples(mapping, Node.ANY, Node.ANY, Node.ANY) );

		final VocabularyMapping vm = new VocabularyMappingWrappingImpl(mappingSet);

		Node s = NodeFactory.createURI("http://example.org/s1");
		Node p = RDF.type.asNode();
		Node o = NodeFactory.createURI("http://example.org/o1");

		final TriplePattern testTp = new TriplePatternImpl(s, p, o);
		SPARQLGraphPattern translation = vm.translateTriplePattern(testTp);


		Set<TriplePattern> translationTriples = new HashSet<>();
		assertTrue(translation instanceof SPARQLUnionPatternImpl);
		for (final SPARQLGraphPattern i : ((SPARQLUnionPatternImpl) translation).getSubPatterns()) {
			assertTrue(i instanceof TriplePattern);
			translationTriples.add( (TriplePattern) i );
		}

		Node p1 = NodeFactory.createURI("http://example.org/subType");
		Node o2 = NodeFactory.createURI("http://example.org/o2");
		Node o3 = NodeFactory.createURI("http://example.org/o3");
		Node o4 = NodeFactory.createURI("http://example.org/o4");

		final TriplePattern expectedTp1 = new TriplePatternImpl(s, p1, o2);
		final TriplePattern expectedTp2 = new TriplePatternImpl(s, p1, o3);
		final TriplePattern expectedTp3 = new TriplePatternImpl(s, p1, o4);

		assertTrue( translationTriples.contains(expectedTp1) );
		assertTrue( translationTriples.contains(expectedTp2) );
		assertTrue( translationTriples.contains(expectedTp3) );

	}

	@Test
	public void applyToSolutionMappingTest() throws IOException {
		// Create mapping rules.
		final String mappingAsTurtle =
				"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
						+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
						+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
						+ "@prefix ex:   <http://example.org/> .  \n"
						+ "ex:n owl:equivalentClass ex:a . "
						+ "ex:o owl:equivalentClass ex:p . "
						+ "ex:m owl:equivalentProperty ex:c . "
						+ "ex:m owl:equivalentProperty ex:d . "
						+ "ex:m owl:equivalentProperty ex:e . "
						+ "ex:f rdfs:subPropertyOf ex:m . "
						+ "ex:g rdfs:subPropertyOf ex:m . "
						+ "ex:p rdfs:subClassOf ex:n . "
						+ "ex:n owl:unionOf (ex:p ex:q) . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);

		final SchemaMappingImplForTests schemaMapping = new SchemaMappingImplForTests(mapping);

		// Create solution mappings.
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node c = NodeFactory.createURI("http://example.org/c");
		final Node g = NodeFactory.createURI("http://example.org/g");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node m = NodeFactory.createURI("http://example.org/m");
		final Node n = NodeFactory.createURI("http://example.org/n");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		final Var z = Var.alloc("z");

		final SolutionMapping solutionMapping = SolutionMappingUtils.createSolutionMapping(x, a, y, g, z, p);
		final Set<SolutionMapping> resultSet = schemaMapping.applyToSolutionMapping(solutionMapping);


		final SolutionMapping sol1 = SolutionMappingUtils.createSolutionMapping(x, n, y, m, z, o);
		final SolutionMapping sol2 = SolutionMappingUtils.createSolutionMapping(x, n, y, m, z, n);

		// See to it that the result set contains the cartesian product and only the cartesian product.
		assertEquals( 2, resultSet.size() );
		assertTrue(resultSet.contains(sol1));
		assertTrue(resultSet.contains(sol2));
	}

	@Test
	public void applyInverseToSolutionMapping() throws IOException {
		// Create mapping rules.
		// This translation is safe only for equivalence-only vocab.mappings
		final String mappingAsTurtle =
				"@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . \n"
						+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . \n"
						+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
						+ "@prefix ex:   <http://example.org/> .  \n"
						+ "ex:n owl:equivalentClass ex:a . "
						+ "ex:o owl:equivalentClass ex:p . "
						+ "ex:m owl:equivalentProperty ex:c . "
						+ "ex:m owl:equivalentProperty ex:d . "
						+ "ex:m owl:equivalentProperty ex:e . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);

		final SchemaMappingImplForTests schemaMapping = new SchemaMappingImplForTests(mapping);

		// Create solution mappings.
		final Node a = NodeFactory.createURI("http://example.org/a");
		final Node c = NodeFactory.createURI("http://example.org/c");
		final Node g = NodeFactory.createURI("http://example.org/g");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node m = NodeFactory.createURI("http://example.org/m");
		final Node n = NodeFactory.createURI("http://example.org/n");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		final Var z = Var.alloc("z");

		final SolutionMapping solutionMapping = SolutionMappingUtils.createSolutionMapping(x, m, y, n, z, o);

		final Set<SolutionMapping> resultSet = schemaMapping.applyInverseToSolutionMapping(solutionMapping);


		// x: c, d, e
		// y: a,
		// z: p
		assertEquals( 3, resultSet.size() );
	}

}
