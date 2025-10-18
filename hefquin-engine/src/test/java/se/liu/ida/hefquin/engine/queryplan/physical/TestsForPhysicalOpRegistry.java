package se.liu.ida.hefquin.engine.queryplan.physical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.physical.TestsForPhysicalOpFactories.ConstructorGPAddAndGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;

import static se.liu.ida.hefquin.engine.queryplan.physical.TestsForPhysicalOpFactories.gpAddConstructor;

public class TestsForPhysicalOpRegistry
{
	@Test
	public void testOpBindJoinWithBoundJoin_gpAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithBoundJoin.Factory() );
		assertSupportForOpBindJoinWithBoundJoin(gpAddConstructor, factory);
	}

	@Test
	public void testOpBindJoinWithBoundJoin_gpOptAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithBoundJoin.Factory() );
		assertSupportForOpBindJoinWithBoundJoin(gpAddConstructor, factory);
	}

	@Test
	public void testOpBindJoinWithUNION_gpAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithUNION.Factory() );
		assertSupportForOpBindJoinWithUNION(gpAddConstructor, factory);
	}

	@Test
	public void testOpBindJoinWithUNION_gpOptAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithUNION.Factory() );
		assertSupportForOpBindJoinWithUNION(gpAddConstructor, factory);
	}


	public void assertSupportForOpBindJoinWithBoundJoin( final ConstructorGPAddAndGPOptAdd ctor,
	                                                     final PhysicalOpRegistry registry ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop = ctor.create( new TestUtils.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables spo = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		// assert type
		assertEquals( PhysicalOpBindJoinWithBoundJoin.class, registry.create(lop, sp).getClass());
		// non-joinable var missing
		assertThrows( NoSuchElementException.class, () -> registry.create(lop, spo) );
	}

	public void assertSupportForOpBindJoinWithUNION( final ConstructorGPAddAndGPOptAdd ctor,
	                                                 final PhysicalOpRegistry registry ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop = ctor.create( new TestUtils.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );

		// assert type
		assertEquals( PhysicalOpBindJoinWithUNION.class, registry.create(lop, sp).getClass());
	}
}
