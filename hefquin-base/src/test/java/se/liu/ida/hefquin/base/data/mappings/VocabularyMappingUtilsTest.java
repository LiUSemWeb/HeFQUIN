package se.liu.ida.hefquin.base.data.mappings;

import static org.junit.Assert.assertEquals;

import java.util.*;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
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
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;

public class VocabularyMappingUtilsTest
{
	final Node s    = NodeFactory.createURI("http://example.org/s");
	final Node s_g  = NodeFactory.createURI("http://example.org/global/s");
	final Node s_l1 = NodeFactory.createURI("http://example.org/local/s1");
	final Node s_l2 = NodeFactory.createURI("http://example.org/local/s2");

	final Node o    = NodeFactory.createURI("http://example.org/o");
	final Node o_g  = NodeFactory.createURI("http://example.org/global/o");
	final Node o_l1 = NodeFactory.createURI("http://example.org/local/o1");
	final Node o_l2 = NodeFactory.createURI("http://example.org/local/o2");

	final Node p    = NodeFactory.createURI("http://example.org/p");
	final Node p_g  = NodeFactory.createURI("http://example.org/global/p");
	final Node p_l1 = NodeFactory.createURI("http://example.org/local/p1");
	final Node p_l2 = NodeFactory.createURI("http://example.org/local/p2");

	// tp
	@Test
	public void tp_ChangeSubject_entity1() {
		final TriplePattern tp = new TriplePatternImpl(s_g, p, o);
		_ChangeSubject_entity_generic1(tp);
	}

	@Test
	public void tp_ChangeSubject_entity2() {
		final TriplePattern tp = new TriplePatternImpl(s_g, p, o);
		_ChangeSubject_entity_generic2(tp);
	}

	@Test
	public void tp_ChangeObject_entity1() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_entity_generic1(tp);
	}

	@Test
	public void tp_ChangeObject_entity2() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_entity_generic2(tp);
	}

	@Test
	public void tp_ChangeObject1() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_generic1(tp);
	}

	@Test
	public void tp_ChangeObject2() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_generic2(tp);
	}

	@Test
	public void tp_ChangePredicate1() {
		final TriplePattern tp = new TriplePatternImpl(s, p_g, o);
		_ChangePredicate_generic1(tp);
	}

	@Test
	public void tp_ChangePredicate2() {
		final TriplePattern tp = new TriplePatternImpl(s, p_g, o);
		_ChangePredicate_generic2(tp);
	}

	// bgp
	@Test
	public void bgp_ChangeSubject_entity1() {
		final TriplePattern tp = new TriplePatternImpl(s_g, p, o);
		_ChangeSubject_entity_generic1( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangeSubject_entity2() {
		final TriplePattern tp = new TriplePatternImpl(s_g, p, o);
		_ChangeSubject_entity_generic2( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangeObject_entity1() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_entity_generic1( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangeObject_entity2() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_entity_generic2( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangeObject1() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_generic1( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangeObject2() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_generic2( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangePredicate1() {
		final TriplePattern tp = new TriplePatternImpl(s, p_g, o);
		_ChangePredicate_generic1( new BGPImpl(tp) );
	}

	@Test
	public void bgp_ChangePredicate2() {
		final TriplePattern tp = new TriplePatternImpl(s, p_g, o);
		_ChangePredicate_generic2( new BGPImpl(tp) );
	}

	// union
	@Test
	public void union_ChangeSubject_entity1() {
		final TriplePattern tp = new TriplePatternImpl(s_g, p, o);
		_ChangeSubject_entity_generic1( new SPARQLUnionPatternImpl(tp) );
	}

	@Test
	public void union_ChangeSubject_entity2() {
		final TriplePattern tp = new TriplePatternImpl(s_g, p, o);
		_ChangeSubject_entity_generic2( new SPARQLUnionPatternImpl(tp));
	}

	@Test
	public void union_ChangeObject_entity1() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_entity_generic1( new SPARQLUnionPatternImpl(tp) );
	}

	@Test
	public void union_ChangeObject_entity2() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_entity_generic2( new SPARQLUnionPatternImpl(tp) );
	}

	@Test
	public void union_ChangeObject1() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_generic1( new SPARQLUnionPatternImpl(tp) );
	}

	@Test
	public void union_ChangeObject2() {
		final TriplePattern tp = new TriplePatternImpl(s, p, o_g);
		_ChangeObject_generic2( new SPARQLUnionPatternImpl(tp) );
	}

	@Test
	public void union_ChangePredicate1() {
		final TriplePattern tp = new TriplePatternImpl(s, p_g, o);
		_ChangePredicate_generic1( new SPARQLUnionPatternImpl(tp) );
	}

	@Test
	public void union_ChangePredicate2() {
		final TriplePattern tp = new TriplePatternImpl(s, p_g, o);
		_ChangePredicate_generic2( new SPARQLUnionPatternImpl(tp) );
	}

	// -------------- helpers --------------

	public void _ChangeSubject_entity_generic1( final SPARQLGraphPattern input ) {
		final String entityMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:s1 owl:sameAs g:s .
			""";
		final TriplePattern tp = new TriplePatternImpl(s_l1, p, o);
		final Set<TriplePattern> expected = Set.of(tp);
		
		final Set<TriplePattern> results = translateAndCollect(input, "", entityMapping);
		assertEquals(expected, results);
	}

	public void _ChangeSubject_entity_generic2( final SPARQLGraphPattern input ) {
		final String entityMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:s1 owl:sameAs g:s .
			l:s2 owl:sameAs g:s .
			""";
		final TriplePattern tp1 = new TriplePatternImpl(s_l1, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2);
		
		final Set<TriplePattern> results = translateAndCollect(input, "", entityMapping);
		assertEquals(expected, results);
	}
	
	public void _ChangeObject_entity_generic1( final SPARQLGraphPattern input ) {
		final String entityMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:o1 owl:sameAs g:o .
			""";
		final TriplePattern tp = new TriplePatternImpl(s, p, o_l1);
		final Set<TriplePattern> expected = Set.of(tp);
		
		final Set<TriplePattern> results = translateAndCollect(input, "", entityMapping);
		assertEquals(expected, results);
	}

	public void _ChangeObject_entity_generic2( final SPARQLGraphPattern input ) {
		final String entityMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:o1 owl:sameAs g:o .
			l:o2 owl:sameAs g:o .
			""";
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2);
		
		final Set<TriplePattern> results = translateAndCollect(input, "", entityMapping);
		assertEquals(expected, results);
	}

	public void _ChangeObject_generic1( final SPARQLGraphPattern input ) {
		final String schemaMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:o1 owl:equivalentClass g:o .
			""";
		final TriplePattern tp = new TriplePatternImpl(s, p, o_l1);
		final Set<TriplePattern> expected = Set.of(tp);
		
		final Set<TriplePattern> results = translateAndCollect(input, schemaMapping, "");
		assertEquals(expected, results);
	}

	public void _ChangeObject_generic2( final SPARQLGraphPattern input ) {
		final String schemaMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:o1 owl:equivalentClass g:o .
			l:o2 owl:equivalentClass g:o .
			""";
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2);
		
		final Set<TriplePattern> results = translateAndCollect(input, schemaMapping, "");
		assertEquals(expected, results);
	}
	
	public void _ChangePredicate_generic1( final SPARQLGraphPattern input ) {
		final String schemaMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:p1 owl:equivalentProperty g:p .
			""";
		final TriplePattern tp = new TriplePatternImpl(s, p_l1, o);
		final Set<TriplePattern> expected = Set.of(tp);
		
		final Set<TriplePattern> results = translateAndCollect(input, schemaMapping, "");
		assertEquals(expected, results);
	}

	public void _ChangePredicate_generic2( final SPARQLGraphPattern input ) {
		final String schemaMapping = """
			@prefix owl:  <http://www.w3.org/2002/07/owl#> .
			@prefix l:    <http://example.org/local/> .
			@prefix g:    <http://example.org/global/> .
			l:p1 owl:equivalentProperty g:p .
			l:p2 owl:equivalentProperty g:p .
			""";
		final TriplePattern tp1 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2);
		
		final Set<TriplePattern> results = translateAndCollect(input, schemaMapping, "");
		assertEquals(expected, results);
	}

	@SuppressWarnings("deprecation")
	protected Set<TriplePattern> translateAndCollect( final SPARQLGraphPattern pattern,
	                                                  final String schemaMapping,
	                                                  final String entityMapping ) {
		final SchemaMapping sm = createSchemaMapping(schemaMapping);
		final EntityMapping em = createEntityMapping(entityMapping);
		final VocabularyMapping vm = new VocabularyMappingWrappingImpl( em, sm );
		
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

		final Set<TriplePattern> results = new HashSet<>();
		for ( final TriplePattern tp : pattern2.getAllMentionedTPs() ) {
			results.add(tp);
		}
		return results;
	}

	protected SchemaMapping createSchemaMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream( mappingAsTurtle, "UTF-8" ), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);
		return new SchemaMappingImpl(g2lMap);
	}

	protected EntityMapping createEntityMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream( mappingAsTurtle, "UTF-8" ), Lang.TURTLE );
		final Map<Node, Set<Node>> g2lMap = EntityMappingReader.read(mapping);
		return new EntityMappingImpl(g2lMap);
	}
}
