package se.liu.ida.hefquin.engine.data.mappings.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.jena.graph.Graph;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.graph.GraphFactory;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;
import se.liu.ida.hefquin.engine.data.impl.SolutionMappingImpl;
import se.liu.ida.hefquin.engine.data.mappings.EntityMapping;
import se.liu.ida.hefquin.engine.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.query.TriplePattern;
import se.liu.ida.hefquin.engine.query.impl.TriplePatternImpl;

public class EntityMappingImplTest
{
	@Test
	public void parseMappingDescriptionTest() throws IOException {
		final String mappingAsTurtle =
				  "@prefix ex:   <http://example.org/> .  \n"
				+ "@prefix owl:  <http://www.w3.org/2002/07/owl#> . \n"
				+ "ex:Robert owl:sameAs ex:Bob . "
				+ "ex:Bobby owl:sameAs ex:Bob . "
				+ "ex:Alibaba owl:sameAs ex:Ali . ";
		final Graph mapping = GraphFactory.createDefaultGraph();
		RDFDataMgr.read(mapping, IOUtils.toInputStream(mappingAsTurtle, "UTF-8"), Lang.TURTLE);
		
		final EntityMappingImpl entityMapping = new EntityMappingImpl(mapping);
		
		// Create global nodes.
		final Node bob = NodeFactory.createURI("http://example.org/Bob");
		final Node ali = NodeFactory.createURI("http://example.org/Ali");
		
		final Set<Node> globalAli = new HashSet<>();
		globalAli.add(ali);
		final Set<Node> globalBob = new HashSet<>();
		globalBob.add(bob);
		
		
		// Create local nodes.
		final Node robert = NodeFactory.createURI("http://example.org/Robert");
		final Node bobby = NodeFactory.createURI("http://example.org/Bobby");
		final Node alibaba = NodeFactory.createURI("http://example.org/Alibaba");
		
		final Set<Node> localAli = new HashSet<>();
		localAli.add(alibaba);
		final Set<Node> localBob = new HashSet<>();
		localBob.add(bobby);
		localBob.add(robert);
		
		// Reference maps.
		final Map<Node, Set<Node>> g2lMap = new HashMap<>();
		g2lMap.put(ali, localAli);
		g2lMap.put(bob, localBob);
		final Map<Node, Set<Node>> l2gMap = new HashMap<>();
		l2gMap.put(alibaba, globalAli);
		l2gMap.put(bobby, globalBob);
		l2gMap.put(robert, globalBob);
		
		assertEquals( g2lMap, entityMapping.g2lMap );
		assertEquals( l2gMap, entityMapping.l2gMap );
	}

	@Test
	public void testApplyToTriplePattern_NoChange1() {
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob = NodeFactory.createURI("http://example.org/local/Bob");

		final Set<Node> lBobSet = Collections.singleton(lBob);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Set<TriplePattern> tps = em.applyToTriplePattern(tp);
		assertEquals( 1, tps.size() );
		assertEquals( tp, tps.iterator().next() );
	}

	@Test
	public void testApplyToTriplePattern_NoChange2() {
		// Tests that URIs in the predicate position are ignored.
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob = NodeFactory.createURI("http://example.org/local/Bob");

		final Set<Node> lBobSet = Collections.singleton(lBob);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = gBob;
		final Node o = NodeFactory.createURI("http://example.org/o");
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Set<TriplePattern> tps = em.applyToTriplePattern(tp);
		assertEquals( 1, tps.size() );
		assertEquals( tp, tps.iterator().next() );
	}

	@Test
	public void testApplyToTriplePattern_ChangeSubject() {
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob = NodeFactory.createURI("http://example.org/local/Bob");

		final Set<Node> lBobSet = Collections.singleton(lBob);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = gBob;
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = NodeFactory.createURI("http://example.org/o");
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Set<TriplePattern> tps = em.applyToTriplePattern(tp);
		assertEquals( 1, tps.size() );

		final Triple tpOut = tps.iterator().next().asJenaTriple();
		assertEquals( lBob, tpOut.getSubject() );
		assertEquals( p,    tpOut.getPredicate() );
		assertEquals( o,    tpOut.getObject() );
	}

	@Test
	public void testApplyToTriplePattern_ChangeSubjectAndObject1() {
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob = NodeFactory.createURI("http://example.org/local/Bob");

		final Set<Node> lBobSet = Collections.singleton(lBob);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = gBob;
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = gBob;
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Set<TriplePattern> tps = em.applyToTriplePattern(tp);
		assertEquals( 1, tps.size() );

		final Triple tpOut = tps.iterator().next().asJenaTriple();
		assertEquals( lBob, tpOut.getSubject() );
		assertEquals( p,    tpOut.getPredicate() );
		assertEquals( lBob, tpOut.getObject() );
	}

	@Test
	public void testApplyToTriplePattern_ChangeSubjectAndObject2() {
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob1 = NodeFactory.createURI("http://example.org/local/Bob1");
		final Node lBob2 = NodeFactory.createURI("http://example.org/local/Bob2");

		final Set<Node> lBobSet = new HashSet<>();
		lBobSet.add(lBob1);
		lBobSet.add(lBob2);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = gBob;
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = gBob;
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Set<TriplePattern> tps = em.applyToTriplePattern(tp);
		assertEquals( 4, tps.size() );

		// check the subjects, each lBob must appear twice
		int cntBob1 = 0;
		int cntBob2 = 0;
		for ( final TriplePattern tpOut : tps ) {
			assertEquals( p, tpOut.asJenaTriple().getPredicate() );
			if ( tpOut.asJenaTriple().getSubject().equals(lBob1) ) cntBob1++;
			if ( tpOut.asJenaTriple().getSubject().equals(lBob2) ) cntBob2++;
		}
		assertEquals(2, cntBob1);
		assertEquals(2, cntBob2);

		// check the objects, each lBob must appear twice
		cntBob1 = 0;
		cntBob2 = 0;
		for ( final TriplePattern tpOut : tps ) {
			if ( tpOut.asJenaTriple().getObject().equals(lBob1) ) cntBob1++;
			if ( tpOut.asJenaTriple().getObject().equals(lBob2) ) cntBob2++;
		}
		assertEquals(2, cntBob1);
		assertEquals(2, cntBob2);
	}

	@Test
	public void testApplyToTriplePattern_ChangeObject1() {
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob = NodeFactory.createURI("http://example.org/local/Bob");

		final Set<Node> lBobSet = Collections.singleton(lBob);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = gBob;
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Set<TriplePattern> tps = em.applyToTriplePattern(tp);
		assertEquals( 1, tps.size() );

		final Triple tpOut = tps.iterator().next().asJenaTriple();
		assertEquals( s,    tpOut.getSubject() );
		assertEquals( p,    tpOut.getPredicate() );
		assertEquals( lBob, tpOut.getObject() );
	}

	@Test
	public void testApplyToTriplePattern_ChangeObject2() {
		final Node gBob  = NodeFactory.createURI("http://example.org/global/Bob");
		final Node lBob1 = NodeFactory.createURI("http://example.org/local/Bob1");
		final Node lBob2 = NodeFactory.createURI("http://example.org/local/Bob2");

		final Set<Node> lBobSet = new HashSet<>();
		lBobSet.add(lBob1);
		lBobSet.add(lBob2);
		final Map<Node, Set<Node>> g2lMap = Collections.singletonMap(gBob, lBobSet);

		final Node s = NodeFactory.createURI("http://example.org/s");
		final Node p = NodeFactory.createURI("http://example.org/p");
		final Node o = gBob;
		final TriplePattern tp = new TriplePatternImpl(s, p, o);

		final EntityMapping em = new EntityMappingImpl(g2lMap);

		final Iterator<TriplePattern> it = em.applyToTriplePattern(tp).iterator();

		assertTrue( it.hasNext() );
		final Triple tpOut1 = it.next().asJenaTriple();

		assertTrue( it.hasNext() );
		final Triple tpOut2 = it.next().asJenaTriple();

		assertFalse( it.hasNext() );

		assertEquals( s, tpOut1.getSubject() );
		assertEquals( s, tpOut2.getSubject() );
		assertEquals( p, tpOut1.getPredicate() );
		assertEquals( p, tpOut2.getPredicate() );

		assertTrue( tpOut1.getObject().equals(lBob1) || tpOut2.getObject().equals(lBob1) );
		assertTrue( tpOut1.getObject().equals(lBob2) || tpOut2.getObject().equals(lBob2) );
	}

	@Test
	public void testApplyMapToSolutionMapping_EmptySolutionMapping() {
		final Node xURI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node yURI1a = NodeFactory.createURI("http://example.org/uri1a");
		final Node yURI1b = NodeFactory.createURI("http://example.org/uri1b");
		final Map<Node, Set<Node>> g2lMap = createG2LMap(xURI1, yURI1a, yURI1b);

		final SolutionMapping emptySolMap = new SolutionMappingImpl();

		// Apply the mapping
		final Set<SolutionMapping> r = EntityMappingImpl.applyMapToSolutionMapping(emptySolMap, g2lMap);

		assertEquals( 1, r.size() );
		assertTrue( r.contains(emptySolMap) );
	}

	@Test
	public void testApplyMapToSolutionMapping_SolutionMappingWithUnmappedURI() {
		final Node xURI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node yURI1a = NodeFactory.createURI("http://example.org/uri1a");
		final Node yURI1b = NodeFactory.createURI("http://example.org/uri1b");
		final Map<Node, Set<Node>> g2lMap = createG2LMap(xURI1, yURI1a, yURI1b);

		final Var var1 = Var.alloc("var1");
		final Node xURI2 = NodeFactory.createURI("http://example.org/uri2");
		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, xURI2);

		// Apply the mapping
		final Set<SolutionMapping> r = EntityMappingImpl.applyMapToSolutionMapping(sm, g2lMap);

		assertEquals( 1, r.size() );
		assertTrue( r.contains(sm) );
	}

	@Test
	public void testApplyMapToSolutionMapping_SolutionMappingWithMappedURI() {
		final Node xURI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node yURI1a = NodeFactory.createURI("http://example.org/uri1a");
		final Node yURI1b = NodeFactory.createURI("http://example.org/uri1b");
		final Map<Node, Set<Node>> g2lMap = createG2LMap(xURI1, yURI1a, yURI1b);

		final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");
		final Node xURI2 = NodeFactory.createURI("http://example.org/uri2");
		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, xURI1, var2, xURI2);

		// Apply the mapping
		final Set<SolutionMapping> r = EntityMappingImpl.applyMapToSolutionMapping(sm, g2lMap);

		assertEquals( 2, r.size() );

		final SolutionMapping expectSM1 = SolutionMappingUtils.createSolutionMapping(var1, yURI1a, var2, xURI2);
		final SolutionMapping expectSM2 = SolutionMappingUtils.createSolutionMapping(var1, yURI1b, var2, xURI2);

		boolean sm1Found = false;
		boolean sm2Found = false;
		for ( final SolutionMapping s : r ) {
			if ( s.equals(expectSM1) ) sm1Found = true;
			if ( s.equals(expectSM2) ) sm2Found = true;
		}

		assertTrue(sm1Found);
		assertTrue(sm2Found);
	}

	@Test
	public void testApplyMapToSolutionMapping_SolutionMappingWithTwoMappedURIs() {
		final Node xURI1 = NodeFactory.createURI("http://example.org/uri1");
		final Node yURI1a = NodeFactory.createURI("http://example.org/uri1a");
		final Node yURI1b = NodeFactory.createURI("http://example.org/uri1b");
		final Map<Node, Set<Node>> g2lMap = createG2LMap(xURI1, yURI1a, yURI1b);

		final Node xURI2 = NodeFactory.createURI("http://example.org/uri2");
		final Node yURI2a = NodeFactory.createURI("http://example.org/uri2a");
		final Node yURI2b = NodeFactory.createURI("http://example.org/uri2b");
		final Set<Node> locals2 = new HashSet<>();
		locals2.add(yURI2a);
		locals2.add(yURI2b);
		g2lMap.put(xURI2, locals2);
		
		final Var var1 = Var.alloc("var1");
		final Var var2 = Var.alloc("var2");
		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(var1, xURI1, var2, xURI2);

		// Apply the mapping
		final Set<SolutionMapping> r = EntityMappingImpl.applyMapToSolutionMapping(sm, g2lMap);

		assertEquals( 4, r.size() );

		final SolutionMapping expectSM1 = SolutionMappingUtils.createSolutionMapping(var1, yURI1a, var2, yURI2a);
		final SolutionMapping expectSM2 = SolutionMappingUtils.createSolutionMapping(var1, yURI1a, var2, yURI2b);
		final SolutionMapping expectSM3 = SolutionMappingUtils.createSolutionMapping(var1, yURI1b, var2, yURI2a);
		final SolutionMapping expectSM4 = SolutionMappingUtils.createSolutionMapping(var1, yURI1b, var2, yURI2b);

		boolean sm1Found = false;
		boolean sm2Found = false;
		boolean sm3Found = false;
		boolean sm4Found = false;
		for ( final SolutionMapping s : r ) {
			if ( s.equals(expectSM1) ) sm1Found = true;
			if ( s.equals(expectSM2) ) sm2Found = true;
			if ( s.equals(expectSM3) ) sm3Found = true;
			if ( s.equals(expectSM4) ) sm4Found = true;
		}

		assertTrue(sm1Found);
		assertTrue(sm2Found);
		assertTrue(sm3Found);
		assertTrue(sm4Found);
	}


	// ------------ helpers --------------

	protected Map<Node, Set<Node>> createG2LMap( final Node g, final Node... localsForG ) {
		final Set<Node> lSet = new HashSet<>( Arrays.asList(localsForG) );

		final Map<Node, Set<Node>> m = new HashMap<>();
		m.put(g, lSet);

		return m;
	}

}
