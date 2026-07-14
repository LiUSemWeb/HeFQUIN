package se.liu.ida.hefquin.testutils;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.graph.GraphFactory;

import se.liu.ida.hefquin.base.data.mappings.EntityMapping;
import se.liu.ida.hefquin.base.data.mappings.SchemaMapping;
import se.liu.ida.hefquin.base.data.mappings.TermMapping;
import se.liu.ida.hefquin.base.data.mappings.impl.EntityMappingImpl;
import se.liu.ida.hefquin.base.data.mappings.impl.EntityMappingReader;
import se.liu.ida.hefquin.base.data.mappings.impl.SchemaMappingImpl;
import se.liu.ida.hefquin.base.data.mappings.impl.SchemaMappingReader;
import se.liu.ida.hefquin.base.data.mappings.impl.VocabularyMappingWrappingImpl;

public class TestUtils
{
	public static VocabularyMappingWrappingImpl createVocabularyMapping() {
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
		return new VocabularyMappingWrappingImpl(em, sm);
	}

	protected static SchemaMapping createSchemaMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );

		final Map<Node, Set<TermMapping>> g2lMap = SchemaMappingReader.read(mapping);
		return new SchemaMappingImpl(g2lMap);
	}

	protected static EntityMapping createEntityMapping( final String mappingAsTurtle ) {
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read( mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE );
		final Map<Node, Set<Node>> g2lMap = EntityMappingReader.read(mapping);
		return new EntityMappingImpl(g2lMap);
	}

	public static Set<String> extractEqualsPairs( final Expr e ) {
		final Set<String> result = new HashSet<>();

		if ( e instanceof E_Equals eq ) {
			result.add( eq.getArg1().toString() + "=" + eq.getArg2().toString() );
		}
		else if ( e instanceof E_LogicalOr or ) {
			result.addAll( extractEqualsPairs(or.getArg1()) );
			result.addAll( extractEqualsPairs(or.getArg2()) );
		}

		return result;
	}
}
