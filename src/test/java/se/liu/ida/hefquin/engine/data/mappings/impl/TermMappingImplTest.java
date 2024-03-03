package se.liu.ida.hefquin.engine.data.mappings.impl;

import static org.junit.Assert.*;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;

import org.junit.Test;

import se.liu.ida.hefquin.engine.data.mappings.TermMapping;

public class TermMappingImplTest {

	@Test
	public void testGetTypeOfRule() {
		final Node type = NodeFactory.createURI("http://example.org/mappingType");
		final TermMapping mapping = new TermMappingImpl(type, NodeFactory.createURI("http://example.org/term"));

		assertEquals(type, mapping.getTypeOfRule());
	}

	@Test
	public void testGetTranslatedTerms_SingleTerm() {
		final Node term = NodeFactory.createURI("http://example.org/term");
		final TermMapping mapping = new TermMappingImpl(NodeFactory.createURI("http://example.org/mappingType"), term);

		final Set<Node> expectedTerms = new HashSet<>();
		expectedTerms.add(term);

		assertEquals(expectedTerms, mapping.getTranslatedTerms());
	}

	@Test
	public void testGetTranslatedTerms_MultipleTerms() {
		final Node term1 = NodeFactory.createURI("http://example.org/term1");
		final Node term2 = NodeFactory.createURI("http://example.org/term2");
		final Set<Node> terms = new HashSet<>();
		terms.add(term1);
		terms.add(term2);
		final TermMapping mapping = new TermMappingImpl(NodeFactory.createURI("http://example.org/mappingType"), terms);

		assertEquals(terms, mapping.getTranslatedTerms());
	}
}
