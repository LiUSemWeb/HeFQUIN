package se.liu.ida.hefquin.base.data.mappings;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.graph.GraphFactory;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Before;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.data.mappings.impl.EntityMappingImpl;
import se.liu.ida.hefquin.base.data.mappings.impl.EntityMappingReader;
import se.liu.ida.hefquin.base.data.mappings.impl.SchemaMappingImpl;
import se.liu.ida.hefquin.base.data.mappings.impl.SchemaMappingReader;
import se.liu.ida.hefquin.base.data.mappings.impl.VocabularyMappingWrappingImpl;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;

public class VocabularyMappingUtilsTest
{
	// Entities (for entity mappings)
	final Node e    = NodeFactory.createURI("http://example.org/e");
	final Node e_g  = NodeFactory.createURI("http://example.org/global/e");
	final Node e_l1 = NodeFactory.createURI("http://example.org/local/e1");
	final Node e_l2 = NodeFactory.createURI("http://example.org/local/e2");
	// Classes (used as subjects in schema mappings)
	final Node s    = NodeFactory.createURI("http://example.org/s");
	final Node s_g  = NodeFactory.createURI("http://example.org/global/s");
	final Node s_l1 = NodeFactory.createURI("http://example.org/local/s1");
	final Node s_l2 = NodeFactory.createURI("http://example.org/local/s2");
	// Predicates (properties, schema mappings)
	final Node p    = NodeFactory.createURI("http://example.org/p");
	final Node p_g  = NodeFactory.createURI("http://example.org/global/p");
	final Node p_l1 = NodeFactory.createURI("http://example.org/local/p1");
	final Node p_l2 = NodeFactory.createURI("http://example.org/local/p2");
	// Classes (used as objects in schema mappings)
	final Node o    = NodeFactory.createURI("http://example.org/o");
	final Node o_g  = NodeFactory.createURI("http://example.org/global/o");
	final Node o_l1 = NodeFactory.createURI("http://example.org/local/o1");
	final Node o_l2 = NodeFactory.createURI("http://example.org/local/o2");

	// Vocabulary mapping
	VocabularyMapping vm;

	@Before
	public void setup() {
		final String entityMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:e1 owl:sameAs g:e .
			l:e2 owl:sameAs g:e .
			""";

		final String schemaMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:s1 owl:equivalentClass g:s .
			l:s2 owl:equivalentClass g:s .
			l:p1 owl:equivalentProperty g:p .
			l:p2 owl:equivalentProperty g:p .
			l:o1 owl:equivalentClass g:o .
			l:o2 owl:equivalentClass g:o .
			""";
		final SchemaMapping sm = createSchemaMapping(schemaMapping);
		final EntityMapping em = createEntityMapping(entityMapping);
		vm = new VocabularyMappingWrappingImpl(em, sm);
	}

	// TriplePattern
	@Test
	public void tp_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	@Test
	public void tp_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	@Test
	public void tp_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	@Test
	public void tp_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	// BGP
	@Test
	public void bgp_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp4 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new BGPImpl(tp1, tp2), expected );
	}

	@Test
	public void bgp_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new BGPImpl(tp1, tp2), expected );
	}

	@Test
	public void bgp_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new BGPImpl(tp1, tp2), expected );
	}

	@Test
	public void bgp_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp4 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation(  new BGPImpl(tp1, tp2), expected );
	}

	// SPARQLGroupPattern
	@Test
	public void group_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp4 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void group_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void group_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void group_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp4 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation(  new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	// SPARQLUnionPattern
	@Test
	public void union_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp4 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void union_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void union_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void union_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp4 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation(  new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	// GenericSPARQLGraphPatternImpl1
	@Test
	public void generic1_subject_entity() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(e_g, p, o));
        el.addTriple(Triple.create(e, p, o));
		final TriplePattern tp1 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	@Test
	public void generic1_object_entity() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(s, p, e_g));
        el.addTriple(Triple.create(s, p, e));
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	@Test
	public void generic1_object_schema() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(s, p, o_g));
        el.addTriple(Triple.create(s, p, o));
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	@Test
	public void generic1_predicate_schema() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(s, p_g, o));
        el.addTriple(Triple.create(s, p, o));
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	// GenericSPARQLGraphPatternImpl2
	@Test
	public void generic2_subject_entity() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(e_g, p, o));
        bp.add(Triple.create(e, p, o));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void generic2_object_entity() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(s, p, e_g));
        bp.add(Triple.create(s, p, e));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void generic2_object_schema() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(s, p, o_g));
        bp.add(Triple.create(s, p, o));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void generic2_predicate_schema() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(s, p_g, o));
        bp.add(Triple.create(s, p, o));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	// -------------- helpers --------------

	@SuppressWarnings("deprecation")
	protected Set<TriplePattern> translateAndCollect( final SPARQLGraphPattern pattern ) {
		final SPARQLGraphPattern pattern2;
		if ( pattern instanceof TriplePattern p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		} 
		else if ( pattern instanceof BGP p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof SPARQLGroupPattern p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof SPARQLUnionPattern p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl1 p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern( p.asJenaOp(), vm );	
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern( p.asJenaOp(), vm );
		}
		else {
			throw new IllegalArgumentException( "Unsupported type of pattern: " + pattern.getClass().getName() );
		}

		return pattern2.getAllMentionedTPs();
	}

	protected void assertMappingTranslation( final SPARQLGraphPattern input,
	                                         final Set<TriplePattern> expected ) {
		final Set<TriplePattern> results = translateAndCollect(input);
		assertEquals(expected, results);
	}

	protected SchemaMapping createSchemaMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);
		return new SchemaMappingImpl(g2lMap);
	}

	protected EntityMapping createEntityMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );
		final Map<Node, Set<Node>> g2lMap = EntityMappingReader.read(mapping);
		return new EntityMappingImpl(g2lMap);
	}
}
