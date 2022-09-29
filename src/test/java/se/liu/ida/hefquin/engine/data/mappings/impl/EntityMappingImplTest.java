package se.liu.ida.hefquin.engine.data.mappings.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.mappings.EntityMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;

public class EntityMappingImplTest {
	@Test
	public void TranslateCartesianProduct() {
		
		// Create solution mappings.
		final Node bob = NodeFactory.createURI("ex:Bob");
		final Node ali = NodeFactory.createURI("ex:Ali");
		final Node charles = NodeFactory.createURI("ex:Chales");
		final Var x = Var.alloc("x");
		final Var y = Var.alloc("y");
		final Var z = Var.alloc("z");
		
		final SolutionMapping solutionMapping = SolutionMappingUtils.createSolutionMapping(x, bob, y, ali, z, charles);

		// Create local vocabulary.
		final Node robert = NodeFactory.createURI("ex:Robert");
		final Node bobby = NodeFactory.createURI("ex:Bobby");
		final Node alibaba = NodeFactory.createURI("ex:Alibaba");
		final Node charlie = NodeFactory.createURI("ex:Charlie");
		final Node carolus = NodeFactory.createURI("ex:Carolus");
		final Node karl = NodeFactory.createURI("ex:Karl");
		
		final Set<Node> localAli = new HashSet<>();
		localAli.add(alibaba);
		final Set<Node> localBob = new HashSet<>();
		localBob.add(bobby);
		localBob.add(robert);
		final Set<Node> localCharles = new HashSet<>();
		localCharles.add(charlie);
		localCharles.add(carolus);
		localCharles.add(karl);
		
		final Map<Node, Set<Node>> g2lMap = new HashMap<>();
		g2lMap.put(ali, localAli);
		g2lMap.put(bob, localBob);
		g2lMap.put(charles, localCharles);
		final Map<Node, Set<Node>> l2gMap = new HashMap<>();
		
		final EntityMapping entityMapping = new EntityMappingImpl(g2lMap, l2gMap);
		
		final Set<SolutionMapping> resultSet = entityMapping.applyToSolutionMapping(solutionMapping);
		
		// Create cartesian product
		final SolutionMapping robertCharlie = SolutionMappingUtils.createSolutionMapping(x, robert, y, alibaba, z, charlie);
		final SolutionMapping robertCarolus = SolutionMappingUtils.createSolutionMapping(x, robert, y, alibaba, z, carolus);
		final SolutionMapping robertKarl = SolutionMappingUtils.createSolutionMapping(x, robert, y, alibaba, z, karl);
		final SolutionMapping bobbyCharlie = SolutionMappingUtils.createSolutionMapping(x, bobby, y, alibaba, z, charlie);
		final SolutionMapping bobbyCarolus = SolutionMappingUtils.createSolutionMapping(x, bobby, y, alibaba, z, carolus);
		final SolutionMapping bobbyKarl = SolutionMappingUtils.createSolutionMapping(x, bobby, y, alibaba, z, karl);
	
		// See to it that the result set contains the cartesian product and only the cartesian product.
		assertEquals( 6, resultSet.size() );
		assertTrue(resultSet.contains(robertCharlie));
		assertTrue(resultSet.contains(robertCarolus));
		assertTrue(resultSet.contains(robertKarl));
		assertTrue(resultSet.contains(bobbyCharlie));
		assertTrue(resultSet.contains(bobbyCarolus));
		assertTrue(resultSet.contains(bobbyKarl));
	}
}
