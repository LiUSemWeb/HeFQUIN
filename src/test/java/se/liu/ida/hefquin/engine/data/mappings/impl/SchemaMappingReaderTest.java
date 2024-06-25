package se.liu.ida.hefquin.engine.data.mappings.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDFS;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.mappings.TermMapping;

public class SchemaMappingReaderTest
{
	@Test
	public void readTest_Equivalences() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l1 owl:equivalentClass ex:g2 . "
				+ "ex:l2 owl:equivalentClass ex:g2 . "
				+ "ex:l3 owl:equivalentProperty ex:g3 . "
				+ "ex:l3 owl:equivalentProperty ex:g4 . "
				+ "ex:l4 owl:equivalentProperty ex:g4 . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);

		// Create global nodes
		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node g2 = NodeFactory.createURI("http://example.org/g2");
		final Node g3 = NodeFactory.createURI("http://example.org/g3");
		final Node g4 = NodeFactory.createURI("http://example.org/g4");

		// Create local nodes
		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final Node l3 = NodeFactory.createURI("http://example.org/l3");
		final Node l4 = NodeFactory.createURI("http://example.org/l4");

		assertEquals( 4, g2lMap.size() );
		assertTrue( g2lMap.containsKey(g1) );
		assertTrue( g2lMap.containsKey(g2) );
		assertTrue( g2lMap.containsKey(g3) );
		assertTrue( g2lMap.containsKey(g4) );

		// checks for g1
		assertEquals( 1, g2lMap.get(g1).size() );
		for ( final TermMapping tm : g2lMap.get(g1) ) {
			assertEquals( g1, tm.getGlobalTerm() );
			assertEquals( OWL.equivalentClass.asNode(), tm.getTypeOfRule() );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			assertTrue( it.hasNext() );
			assertEquals( l1, it.next() );
			assertFalse( it.hasNext() );
		}

		// checks for g2
		int cnt = 0;
		boolean l1Found = false;
		boolean l2Found = false;
		for ( final TermMapping tm : g2lMap.get(g2) ) {
			assertEquals( g2, tm.getGlobalTerm() );
			assertEquals( OWL.equivalentClass.asNode(), tm.getTypeOfRule() );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			while ( it.hasNext() ) {
				cnt++;
				final Node n = it.next();
				assertTrue( n.equals(l1) || n.equals(l2) );

				if ( n.equals(l1) ) l1Found = true;
				if ( n.equals(l2) ) l2Found = true;
			}
		}

		assertEquals(2, cnt);
		assertTrue(l1Found);
		assertTrue(l2Found);

		// checks for g3
		assertEquals( 1, g2lMap.get(g3).size() );
		for ( final TermMapping tm : g2lMap.get(g3) ) {
			assertEquals( g3, tm.getGlobalTerm() );
			assertEquals( OWL.equivalentProperty.asNode(), tm.getTypeOfRule() );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			assertTrue( it.hasNext() );
			assertEquals( l3, it.next() );
			assertFalse( it.hasNext() );
		}

		// checks for g4
		cnt = 0;
		boolean l3Found = false;
		boolean l4Found = false;
		for ( final TermMapping tm : g2lMap.get(g4) ) {
			assertEquals( g4, tm.getGlobalTerm() );
			assertEquals( OWL.equivalentProperty.asNode(), tm.getTypeOfRule() );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			while ( it.hasNext() ) {
				cnt++;
				final Node n = it.next();
				assertTrue( n.equals(l3) || n.equals(l4) );

				if ( n.equals(l3) ) l3Found = true;
				if ( n.equals(l4) ) l4Found = true;
			}
		}

		assertEquals(2, cnt);
		assertTrue(l3Found);
		assertTrue(l4Found);
	}

	@Test
	public void readTest_UnionAndEquivalence() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g . "
				+ "ex:g owl:unionOf (ex:l2 ex:l3) . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);

		// Create global node
		final Node g = NodeFactory.createURI("http://example.org/g");

		// Create local nodes
		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final Node l3 = NodeFactory.createURI("http://example.org/l3");

		assertEquals( 1, g2lMap.size() );
		assertTrue( g2lMap.containsKey(g) );

		assertEquals( 2, g2lMap.get(g).size() );
		for ( final TermMapping tm : g2lMap.get(g) ) {
			assertEquals( g, tm.getGlobalTerm() );

			final Node type = tm.getTypeOfRule();
			assertTrue( OWL.equivalentClass.asNode().equals(type) || OWL.unionOf.asNode().equals(type) );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			if ( OWL.unionOf.asNode().equals(type) ) {
				int cnt = 0;
				boolean l2Found = false;
				boolean l3Found = false;
				while ( it.hasNext() ) {
					cnt++;
					final Node n = it.next();
					assertTrue( n.equals(l2) || n.equals(l3) );

					if ( n.equals(l2) ) l2Found = true;
					if ( n.equals(l3) ) l3Found = true;
				}

				assertEquals(2, cnt);
				assertTrue(l2Found);
				assertTrue(l3Found);
			}
			else { // type is owl:equivalentClass
				assertTrue( it.hasNext() );
				assertEquals( l1, it.next() );
				assertFalse( it.hasNext() );
			}
		}
	}

	@Test
	public void readTest_SubAndEquivalence() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 rdfs:subClassOf     ex:g1 . "
				+ "ex:l3 rdfs:subClassOf     ex:g1 . "
				+ "ex:l4 rdfs:subPropertyOf ex:g2 . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);

		// Create global node
		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node g2 = NodeFactory.createURI("http://example.org/g2");

		// Create local nodes
		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final Node l3 = NodeFactory.createURI("http://example.org/l3");
		final Node l4 = NodeFactory.createURI("http://example.org/l4");

		assertEquals( 2, g2lMap.size() );
		assertTrue( g2lMap.containsKey(g1) );
		assertTrue( g2lMap.containsKey(g2) );

		// checking for g1
		int cnt = 0;
		boolean l2Found = false;
		boolean l3Found = false;
		for ( final TermMapping tm : g2lMap.get(g1) ) {
			assertEquals( g1, tm.getGlobalTerm() );

			final Node type = tm.getTypeOfRule();
			assertTrue( OWL.equivalentClass.asNode().equals(type) || RDFS.subClassOf.asNode().equals(type) );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			if ( RDFS.subClassOf.asNode().equals(type) ) {
				while ( it.hasNext() ) {
					cnt++;
					final Node n = it.next();
					assertTrue( n.equals(l2) || n.equals(l3) );

					if ( n.equals(l2) ) l2Found = true;
					if ( n.equals(l3) ) l3Found = true;
				}
			}
			else { // type is owl:equivalentClass
				assertTrue( it.hasNext() );
				assertEquals( l1, it.next() );
				assertFalse( it.hasNext() );
			}
		}

		assertEquals(2, cnt);
		assertTrue(l2Found);
		assertTrue(l3Found);

		// checks for g2
		assertEquals( 1, g2lMap.get(g2).size() );
		for ( final TermMapping tm : g2lMap.get(g2) ) {
			assertEquals( g2, tm.getGlobalTerm() );
			assertEquals( RDFS.subPropertyOf.asNode(), tm.getTypeOfRule() );

			final Iterator<Node> it = tm.getLocalTerms().iterator();
			assertTrue( it.hasNext() );
			assertEquals( l4, it.next() );
			assertFalse( it.hasNext() );
		}
	}

}
