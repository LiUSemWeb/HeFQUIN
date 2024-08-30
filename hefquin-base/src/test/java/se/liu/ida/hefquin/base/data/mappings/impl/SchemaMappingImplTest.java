package se.liu.ida.hefquin.base.data.mappings.impl;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.mappings.SchemaMapping;
import se.liu.ida.hefquin.base.data.mappings.TermMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;

import java.io.IOException;
import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class SchemaMappingImplTest
{
	@Test
	public void testApplyToTriplePattern_NoChange() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentProperty ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node s = NodeFactory.createURI("http://example.org/s1");
		final Node p = RDF.type.asNode();
		final Node o = NodeFactory.createURI("http://example.org/o1");

		final TriplePattern tp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern resultingPattern = m.applyToTriplePattern(tp);

		assertEquals( tp, resultingPattern );
	}

	@Test
	public void testApplyToTriplePattern_ChangePredicate1() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentProperty ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/g2");
		final Node o = NodeFactory.createURI("http://example.org/o");

		final TriplePattern tp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern resultingPattern = m.applyToTriplePattern(tp);

		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final TriplePattern expected = new TriplePatternImpl(s, l2, o);

		assertEquals( expected, resultingPattern );
	}

	@Test
	public void testApplyToTriplePattern_ChangePredicate2() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 rdfs:subPropertyOf     ex:g2 . "
				+ "ex:l3 owl:equivalentProperty ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/g2");
		final Node o = NodeFactory.createURI("http://example.org/o");

		final TriplePattern tp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern resultingPattern = m.applyToTriplePattern(tp);

		assertTrue( resultingPattern instanceof SPARQLUnionPattern );
		final SPARQLUnionPattern resultingUnionPattern = (SPARQLUnionPattern) resultingPattern;

		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final Node l3 = NodeFactory.createURI("http://example.org/l3");
		final TriplePattern expected1 = new TriplePatternImpl(s, l2, o);
		final TriplePattern expected2 = new TriplePatternImpl(s, l3, o);

		int cnt = 0;
		boolean expected1Found = false;
		boolean expected2Found = false;
		for ( final SPARQLGraphPattern sub : resultingUnionPattern.getSubPatterns() ) {
			cnt++;
			if ( sub.equals(expected1) ) expected1Found = true;
			if ( sub.equals(expected2) ) expected2Found = true;
		}
		assertEquals( 2, cnt );
		assertEquals( true, expected1Found );
		assertEquals( true, expected2Found );
	}

	@Test
	public void testApplyToTriplePattern_ChangeObject1() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentProperty ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node s = NodeFactory.createURI("http://example.org/s1");
		final Node p = RDF.type.asNode();
		final Node o = NodeFactory.createURI("http://example.org/g1");

		final TriplePattern tp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern resultingPattern = m.applyToTriplePattern(tp);

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final TriplePattern expected = new TriplePatternImpl(s, p, l1);

		assertEquals( expected, resultingPattern );
	}

	@Test
	public void testApplyToTriplePattern_ChangeObject2_1() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:g1 owl:unionOf (ex:l1 ex:l2) . "
				+ "ex:l3 owl:equivalentProperty ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node s = NodeFactory.createURI("http://example.org/s1");
		final Node p = RDF.type.asNode();
		final Node o = NodeFactory.createURI("http://example.org/g1");

		final TriplePattern tp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern resultingPattern = m.applyToTriplePattern(tp);

		assertTrue( resultingPattern instanceof SPARQLUnionPattern );
		final SPARQLUnionPattern resultingUnionPattern = (SPARQLUnionPattern) resultingPattern;

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final TriplePattern expected1 = new TriplePatternImpl(s, p, l1);
		final TriplePattern expected2 = new TriplePatternImpl(s, p, l2);

		int cnt = 0;
		boolean expected1Found = false;
		boolean expected2Found = false;
		for ( final SPARQLGraphPattern sub : resultingUnionPattern.getSubPatterns() ) {
			cnt++;
			if ( sub.equals(expected1) ) expected1Found = true;
			if ( sub.equals(expected2) ) expected2Found = true;
		}
		assertEquals( 2, cnt );
		assertEquals( true, expected1Found );
		assertEquals( true, expected2Found );
	}

	@Test
	public void testApplyToTriplePattern_ChangeObject2_2() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 rdfs:subClassOf     ex:g1 . "
				+ "ex:l3 owl:equivalentProperty ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node s = NodeFactory.createURI("http://example.org/s1");
		final Node p = RDF.type.asNode();
		final Node o = NodeFactory.createURI("http://example.org/g1");

		final TriplePattern tp = new TriplePatternImpl(s, p, o);
		final SPARQLGraphPattern resultingPattern = m.applyToTriplePattern(tp);

		assertTrue( resultingPattern instanceof SPARQLUnionPattern );
		final SPARQLUnionPattern resultingUnionPattern = (SPARQLUnionPattern) resultingPattern;

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final TriplePattern expected1 = new TriplePatternImpl(s, p, l1);
		final TriplePattern expected2 = new TriplePatternImpl(s, p, l2);

		int cnt = 0;
		boolean expected1Found = false;
		boolean expected2Found = false;
		for ( final SPARQLGraphPattern sub : resultingUnionPattern.getSubPatterns() ) {
			cnt++;
			if ( sub.equals(expected1) ) expected1Found = true;
			if ( sub.equals(expected2) ) expected2Found = true;
		}
		assertEquals( 2, cnt );
		assertEquals( true, expected1Found );
		assertEquals( true, expected2Found );
	}

	@Test
	public void testApplyToSolutionMapping_NoChange() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentClass ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, x2);
		final Set<SolutionMapping> result = m.applyToSolutionMapping(sm);

		assertEquals( 1, result.size() );

		assertEquals( sm, result.iterator().next() );
	}

	@Test
	public void testApplyToSolutionMapping_ChangeOneVar() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentClass ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node x = NodeFactory.createURI("http://example.org/x");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, g1, v2, x);
		final Set<SolutionMapping> result = m.applyToSolutionMapping(sm);

		assertEquals( 1, result.size() );

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final SolutionMapping expected = SolutionMappingUtils.createSolutionMapping(v1, l1, v2, x);

		assertEquals( expected, result.iterator().next() );
	}

	@Test
	public void testApplyToSolutionMapping_ChangeOneVar2() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentClass ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node x = NodeFactory.createURI("http://example.org/x");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, g1, v2, x);
		final Set<SolutionMapping> result = m.applyToSolutionMapping(sm);

		assertEquals( 2, result.size() );

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(v1, l1, v2, x);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(v1, l2, v2, x);

		boolean sm1Found = false;
		boolean sm2Found = false;
		for ( final SolutionMapping smx : result ) {
			if ( smx.equals(sm1) ) sm1Found = true;
			if ( smx.equals(sm2) ) sm2Found = true;
		}

		assertEquals(true, sm1Found);
		assertEquals(true, sm2Found);
	}

	@Test
	public void testApplyToSolutionMapping_ChangeTwoVars1() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1 . "
				+ "ex:l2 owl:equivalentClass ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node g2 = NodeFactory.createURI("http://example.org/g2");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, g1, v2, g2);
		final Set<SolutionMapping> result = m.applyToSolutionMapping(sm);

		assertEquals( 1, result.size() );

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final SolutionMapping expected = SolutionMappingUtils.createSolutionMapping(v1, l1, v2, l2);

		assertEquals( expected, result.iterator().next() );
	}

	@Test
	public void testApplyToSolutionMapping_ChangeTwoVars2() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1a owl:equivalentClass ex:g1 . "
				+ "ex:l1b owl:equivalentClass ex:g1 . "
				+ "ex:l2a owl:equivalentClass ex:g2 . "
				+ "ex:l2b owl:equivalentClass ex:g2 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node g2 = NodeFactory.createURI("http://example.org/g2");
		final Node x = NodeFactory.createURI("http://example.org/x");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Var v3 = Var.alloc("v3");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, g1, v3, x, v2, g2);
		final Set<SolutionMapping> result = m.applyToSolutionMapping(sm);

		assertEquals( 4, result.size() );

		final Node l1a = NodeFactory.createURI("http://example.org/l1a");
		final Node l1b = NodeFactory.createURI("http://example.org/l1b");
		final Node l2a = NodeFactory.createURI("http://example.org/l2a");
		final Node l2b = NodeFactory.createURI("http://example.org/l2b");
		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(v1, l1a, v2, l2a, v3, x);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(v1, l1a, v2, l2b, v3, x);
		final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(v1, l1b, v2, l2a, v3, x);
		final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(v1, l1b, v2, l2b, v3, x);

		boolean sm1Found = false;
		boolean sm2Found = false;
		boolean sm3Found = false;
		boolean sm4Found = false;
		for ( final SolutionMapping smx : result ) {
			if ( smx.equals(sm1) ) sm1Found = true;
			if ( smx.equals(sm2) ) sm2Found = true;
			if ( smx.equals(sm3) ) sm3Found = true;
			if ( smx.equals(sm4) ) sm4Found = true;
		}

		assertEquals(true, sm1Found);
		assertEquals(true, sm2Found);
		assertEquals(true, sm3Found);
		assertEquals(true, sm4Found);
	}

	@Test
	public void testApplyInverseToSolutionMapping_ChangeTwoVars2() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:l1 owl:equivalentClass ex:g1a . "
				+ "ex:l1 owl:equivalentClass ex:g1b . "
				+ "ex:l2 owl:equivalentClass ex:g2a . "
				+ "ex:l2 owl:equivalentClass ex:g2b . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node l2 = NodeFactory.createURI("http://example.org/l2");
		final Node x = NodeFactory.createURI("http://example.org/x");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Var v3 = Var.alloc("v3");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v3, x, v1, l1, v2, l2);
		final Set<SolutionMapping> result = m.applyInverseToSolutionMapping(sm);

		assertEquals( 4, result.size() );

		final Node g1a = NodeFactory.createURI("http://example.org/g1a");
		final Node g1b = NodeFactory.createURI("http://example.org/g1b");
		final Node g2a = NodeFactory.createURI("http://example.org/g2a");
		final Node g2b = NodeFactory.createURI("http://example.org/g2b");
		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(v1, g1a, v2, g2a, v3, x);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(v1, g1a, v2, g2b, v3, x);
		final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(v1, g1b, v2, g2a, v3, x);
		final SolutionMapping sm4 = SolutionMappingUtils.createSolutionMapping(v1, g1b, v2, g2b, v3, x);

		boolean sm1Found = false;
		boolean sm2Found = false;
		boolean sm3Found = false;
		boolean sm4Found = false;
		for ( final SolutionMapping smx : result ) {
			if ( smx.equals(sm1) ) sm1Found = true;
			if ( smx.equals(sm2) ) sm2Found = true;
			if ( smx.equals(sm3) ) sm3Found = true;
			if ( smx.equals(sm4) ) sm4Found = true;
		}

		assertEquals(true, sm1Found);
		assertEquals(true, sm2Found);
		assertEquals(true, sm3Found);
		assertEquals(true, sm4Found);
	}

	@Test
	public void testApplyInverseToSolutionMapping_ChangeOneVar3() throws IOException {
		final String mappingAsTurtle =
				  "@prefix rdf:  <http://www.w3.org/1999/02/22-rdf-syntax-ns#> . "
				+ "@prefix rdfs: <http://www.w3.org/2000/01/rdf-schema#> . "
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . "
				+ "@prefix ex:   <http://example.org/> . "
				+ "ex:g1 owl:unionOf (ex:l1 ex:l2) . "
				+ "ex:l1 owl:equivalentClass ex:g2 . "
				+ "ex:l1 rdfs:subClassOf ex:g3 . "
				+ "ex:l2 rdfs:subClassOf ex:g3 . ";

		final SchemaMapping m = createMapping(mappingAsTurtle);

		final Node l1 = NodeFactory.createURI("http://example.org/l1");
		final Node x1 = NodeFactory.createURI("http://example.org/x1");
		final Node x2 = NodeFactory.createURI("http://example.org/x2");
		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Var v3 = Var.alloc("v3");

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, l1, v3, x2);
		final Set<SolutionMapping> result = m.applyInverseToSolutionMapping(sm);

		assertEquals( 3, result.size() );

		final Node g1 = NodeFactory.createURI("http://example.org/g1");
		final Node g2 = NodeFactory.createURI("http://example.org/g2");
		final Node g3 = NodeFactory.createURI("http://example.org/g3");
		final SolutionMapping sm1 = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, g1, v3, x2);
		final SolutionMapping sm2 = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, g2, v3, x2);
		final SolutionMapping sm3 = SolutionMappingUtils.createSolutionMapping(v1, x1, v2, g3, v3, x2);

		boolean sm1Found = false;
		boolean sm2Found = false;
		boolean sm3Found = false;
		for ( final SolutionMapping smx : result ) {
			if ( smx.equals(sm1) ) sm1Found = true;
			if ( smx.equals(sm2) ) sm2Found = true;
			if ( smx.equals(sm3) ) sm3Found = true;
		}

		assertEquals(true, sm1Found);
		assertEquals(true, sm2Found);
		assertEquals(true, sm3Found);
	}


	// -------------- helpers --------------

	protected SchemaMappingImpl createMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);
		return new SchemaMappingImpl(g2lMap);
	}

}
