package se.liu.ida.hefquin.engine.queryplan.physical;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.federation.FederationMember;

public class TestsForPhysicalOpFactories {
    interface LogicalOpConstructor extends BiFunction<FederationMember, SPARQLGraphPattern, LogicalOperator> {
        // first arg is the federation member type your constructors accept
    }

	@Test
	public void testPhysicalOpBindJoinWithBoundJoin_gpAdd() {
		assertSupportForOpBindJoinWithBoundJoin( LogicalOpGPAdd::new );
	}

	@Test
	public void testPhysicalOpBindJoinWithBoundJoin_gpOptAdd() {
		assertSupportForOpBindJoinWithBoundJoin( LogicalOpGPOptAdd::new );
	}

	@Test
	public void testPhysicalOpBindJoinWithUNION_gpAdd() {
		assertSupportForOpBindJoinWithUNION( LogicalOpGPAdd::new );
	}

	@Test
	public void testPhysicalOpBindJoinWithUNION_gpOptAdd() {
		assertSupportForOpBindJoinWithUNION( LogicalOpGPOptAdd::new );
	}

	public void assertSupportForOpBindJoinWithBoundJoin( final LogicalOpConstructor logicalOpConstructor ){
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithBoundJoin.Factory();

		final String queryString = "SELECT * WHERE { ?s ?p ?o OPTIONAL { ?_s ?_p ?o} }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop_sparql = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );
		final LogicalOperator lop_tpf = logicalOpConstructor.apply( new TestUtils.TPFServerForTest(), p );
		final LogicalOperator lop_brtpf = logicalOpConstructor.apply( new TestUtils.BRTPFServerForTest(), p );

		final ExpectedVariables sp1 = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables po1 = TestUtils.getExpectedVariables( List.of("p", "o"), List.of() );
		final ExpectedVariables so1 = TestUtils.getExpectedVariables( List.of("s", "o"), List.of() );
		final ExpectedVariables spo1 = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		final ExpectedVariables sp2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "p") );
		final ExpectedVariables po2 = TestUtils.getExpectedVariables( List.of(), List.of("p", "o") );
		final ExpectedVariables so2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "o") );
		final ExpectedVariables spo2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "p", "o") );

		// certain vars
		assertTrue(  factory.supports(lop_sparql, sp1) );
		assertTrue(  factory.supports(lop_sparql, po1) );
		assertTrue(  factory.supports(lop_sparql, so1) );
		assertFalse( factory.supports(lop_sparql, spo1) );
		// possible vars
		assertTrue(  factory.supports(lop_sparql, sp2) );
		assertTrue(  factory.supports(lop_sparql, po2) );
		assertTrue(  factory.supports(lop_sparql, so2) );
		assertFalse( factory.supports(lop_sparql, spo2) );
		// unsupported federation member types
		assertFalse( factory.supports(lop_tpf,    sp1) );
		assertFalse( factory.supports(lop_brtpf,  sp1) );
	}

	public void assertSupportForOpBindJoinWithUNION( final LogicalOpConstructor logicalOpConstructor ){
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithUNION.Factory();

		final String queryString = "SELECT * WHERE { ?s ?p ?o OPTIONAL { ?_s ?_p ?o} }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop_sparql = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );
		final LogicalOperator lop_tpf = logicalOpConstructor.apply( new TestUtils.TPFServerForTest(), p );
		final LogicalOperator lop_brtpf = logicalOpConstructor.apply( new TestUtils.BRTPFServerForTest(), p );

		final ExpectedVariables sp1 = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables po1 = TestUtils.getExpectedVariables( List.of("p", "o"), List.of() );
		final ExpectedVariables so1 = TestUtils.getExpectedVariables( List.of("s", "o"), List.of() );
		final ExpectedVariables spo1 = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		final ExpectedVariables sp2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "p") );
		final ExpectedVariables po2 = TestUtils.getExpectedVariables( List.of(), List.of("p", "o") );
		final ExpectedVariables so2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "o") );
		final ExpectedVariables spo2 = TestUtils.getExpectedVariables( List.of(), List.of("s", "p", "o") );

		// certain vars
		assertTrue( factory.supports(lop_sparql, sp1) );
		assertTrue( factory.supports(lop_sparql, po1) );
		assertTrue( factory.supports(lop_sparql, so1) );
		assertTrue( factory.supports(lop_sparql, spo1) );
		// possible vars
		assertTrue( factory.supports(lop_sparql, sp2) );
		assertTrue( factory.supports(lop_sparql, po2) );
		assertTrue( factory.supports(lop_sparql, so2) );
		assertTrue( factory.supports(lop_sparql, spo2) );
		// unsupported federation member types
		assertFalse( factory.supports(lop_tpf,    sp1) );
		assertFalse( factory.supports(lop_brtpf,  sp1) );
	}
}
