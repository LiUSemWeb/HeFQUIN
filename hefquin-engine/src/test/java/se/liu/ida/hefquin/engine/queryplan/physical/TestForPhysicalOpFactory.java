package se.liu.ida.hefquin.engine.queryplan.physical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.TestsForPhysicalOpProviders.LogicalOpConstructor;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;

public class TestForPhysicalOpFactory
{
	@Test
	public void testOpBindJoinWithBoundJoin_gpAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithBoundJoin.Factory() );
		assertSupportForOpBindJoinWithBoundJoin( LogicalOpGPAdd::new, factory );
	}

	@Test
	public void testOpBindJoinWithBoundJoin_gpOptAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithBoundJoin.Factory() );
		assertSupportForOpBindJoinWithBoundJoin( LogicalOpGPAdd::new, factory );
	}

	@Test
	public void testOpBindJoinWithUNION_gpAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithUNION.Factory() );
		assertSupportForOpBindJoinWithUNION( LogicalOpGPAdd::new, factory );
	}

	@Test
	public void testOpBindJoinWithUNION_gpOptAdd(){
		final PhysicalOpRegistry factory = new PhysicalOpRegistry()
			.register( new PhysicalOpBindJoinWithUNION.Factory() );
		assertSupportForOpBindJoinWithUNION( LogicalOpGPAdd::new, factory );
	}


	public void assertSupportForOpBindJoinWithBoundJoin( final LogicalOpConstructor logicalOpConstructor,
	                                                     final PhysicalOpRegistry registry ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables spo = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		// wrong type returned
		assertEquals( PhysicalOpBindJoinWithBoundJoin.class, registry.create(lop, sp).getClass());
		// non-joinable var missing
		assertThrows( UnsupportedOperationException.class, () -> registry.create(lop, spo) );
	}

	public void assertSupportForOpBindJoinWithUNION( final LogicalOpConstructor logicalOpConstructor,
	                                                 final PhysicalOpRegistry registry ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );

		// wrong type returned
		assertEquals( PhysicalOpBindJoinWithUNION.class, registry.create(lop, sp).getClass());
	}
}
