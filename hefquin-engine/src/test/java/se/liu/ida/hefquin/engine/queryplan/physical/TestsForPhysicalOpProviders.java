package se.liu.ida.hefquin.engine.queryplan.physical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;

public class TestsForPhysicalOpProviders {
	@Test
	public void testProviderForPhysicalOpBindJoinWithBoundJoin_NonJoiningVar(){
		final PhysicalOpProvider provider = new PhysicalOpBindJoinWithBoundJoin.Provider();

		final String queryString = "SELECT * WHERE { ?s ?p ?o OPTIONAL { ?_s ?_p ?o} }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOpGPAdd lop = new LogicalOpGPAdd( new TestUtils.SPARQLEndpointForTest(), p );
		
		final ExpectedVariables sp1 = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables po1 = TestUtils.getExpectedVariables( List.of("p", "o"), List.of() );
		final ExpectedVariables so1 = TestUtils.getExpectedVariables( List.of("s", "o"), List.of() );
		final ExpectedVariables spo1 = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		final ExpectedVariables sp2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "p") );
		final ExpectedVariables po2 = TestUtils.getExpectedVariables( List.of(), List.of("p", "o") );
		final ExpectedVariables so2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "o") );
		final ExpectedVariables spo2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "p", "o") );

		// certain vars
		assertTrue( provider.supports(lop, sp1) );
		assertTrue( provider.supports(lop, po1) );
		assertTrue( provider.supports(lop, so1) );
		assertFalse( provider.supports(lop, spo1) );
		// possible vars
		assertTrue( provider.supports(lop, sp2) );
		assertTrue( provider.supports(lop, po2) );
		assertTrue( provider.supports(lop, so2) );
		assertFalse( provider.supports(lop, spo2) );
	}

	@Test
	public void testProviderForPhysicalOpBindJoinWithBoundJoin_fm(){
		final PhysicalOpProvider provider = new PhysicalOpBindJoinWithBoundJoin.Provider();

		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);

		final LogicalOpGPAdd lop_sparql = new LogicalOpGPAdd( new TestUtils.SPARQLEndpointForTest(), p );
		final LogicalOpGPAdd lop_tpf = new LogicalOpGPAdd( new TestUtils.TPFServerForTest(), p );
		final LogicalOpGPAdd lop_brtfp = new LogicalOpGPAdd( new TestUtils.BRTPFServerForTest(), p );
		
		final ExpectedVariables vars = TestUtils.getExpectedVariables( List.of(), List.of() );

		assertTrue(  provider.supports(lop_sparql, vars) );
		assertFalse( provider.supports(lop_tpf,    vars) );
		assertFalse( provider.supports(lop_brtfp,  vars) );
	}
}
