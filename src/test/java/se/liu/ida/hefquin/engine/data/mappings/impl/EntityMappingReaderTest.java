package se.liu.ida.hefquin.engine.data.mappings.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

public class EntityMappingReaderTest
{
	@Test
	public void readTest() throws IOException {
		final String mappingAsTurtle =
				  "@prefix ex:   <http://example.org/> .  \n"
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
				+ "ex:Robert   owl:sameAs  ex:Bob . "
				+ "ex:Bobby    owl:sameAs  ex:Bob . "
				+ "ex:Alibaba  owl:sameAs  ex:Ali . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);

		final Map<Node, Set<Node>> g2lMap = EntityMappingReader.read(mapping);

		assertEquals( 2, g2lMap.size() );

		final Node bob = NodeFactory.createURI("http://example.org/Bob");
		final Node ali = NodeFactory.createURI("http://example.org/Ali");

		assertTrue( g2lMap.containsKey(ali) );
		assertTrue( g2lMap.containsKey(bob) );
		assertEquals( 1, g2lMap.get(ali).size() );
		assertEquals( 2, g2lMap.get(bob).size() );

		final Set<Node> expectedAli = new HashSet<>();
		expectedAli.add( NodeFactory.createURI("http://example.org/Alibaba") );

		final Set<Node> expectedBob = new HashSet<>();
		expectedBob.add( NodeFactory.createURI("http://example.org/Bobby") );
		expectedBob.add( NodeFactory.createURI("http://example.org/Robert") );

		assertEquals( expectedAli, g2lMap.get(ali) );
		assertEquals( expectedBob, g2lMap.get(bob) );
	}

}
