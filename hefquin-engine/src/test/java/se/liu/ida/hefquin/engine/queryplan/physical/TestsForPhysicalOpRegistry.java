package se.liu.ida.hefquin.engine.queryplan.physical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.EngineTestBase;
import se.liu.ida.hefquin.engine.queryplan.logical.UnaryLogicalOp;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.TestsForPhysicalOpFactories.ConstructorGPAddAndGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinSPARQL;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

import static se.liu.ida.hefquin.engine.queryplan.physical.TestsForPhysicalOpFactories.gpAddConstructor;

public class TestsForPhysicalOpRegistry
{
	@Test
	public void testOpRequest(){
		final PhysicalOpRegistry registry = new PhysicalOpRegistry();
		registry.register( PhysicalOpRequest.getFactory() );

		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1( new ElementTriplesBlock() );
		final LogicalOpRequest<?,?> lop = new LogicalOpRequest<>( new EngineTestBase.SPARQLEndpointForTest(),
		                                                          new SPARQLRequestImpl(p) );

		assertEquals( PhysicalOpRequest.class, registry.create(lop).getClass() );
	}

	@Test
	public void testOpBindJoinWithBoundJoin_gpAdd(){
		final PhysicalOpRegistry registry = new PhysicalOpRegistry();
		registry.register( new PhysicalOpBindJoinSPARQL.Factory("VARIABLE_RENAMING",
		                                                        false,
		                                                        30) );
		assertSupportForOpBindJoinWithBoundJoin(gpAddConstructor, registry);
	}

	@Test
	public void testOpBindJoinWithBoundJoin_gpOptAdd(){
		final PhysicalOpRegistry registry = new PhysicalOpRegistry();
		registry.register( new PhysicalOpBindJoinSPARQL.Factory("VARIABLE_RENAMING",
		                                                        false,
		                                                        30) );
		assertSupportForOpBindJoinWithBoundJoin(gpAddConstructor, registry);
	}

	@Test
	public void testOpBindJoinWithUNION_gpAdd(){
		final PhysicalOpRegistry registry = new PhysicalOpRegistry();
		registry.register( new PhysicalOpBindJoinSPARQL.Factory("UNION_BASED",
		                                                        false,
		                                                        30) );
		assertSupportForOpBindJoinWithUNION(gpAddConstructor, registry);
	}

	@Test
	public void testOpBindJoinWithUNION_gpOptAdd(){
		final PhysicalOpRegistry registry = new PhysicalOpRegistry();
		registry.register( new PhysicalOpBindJoinSPARQL.Factory("UNION_BASED",
		                                                        false,
		                                                        30) );
		assertSupportForOpBindJoinWithUNION(gpAddConstructor, registry);
	}


	public void assertSupportForOpBindJoinWithBoundJoin( final ConstructorGPAddAndGPOptAdd ctor,
	                                                     final PhysicalOpRegistry registry ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final UnaryLogicalOp lop = ctor.create( new EngineTestBase.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables spo = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		// assert type
		assertEquals( PhysicalOpBindJoinSPARQL.class, registry.create(lop, sp).getClass());
		// non-joinable var missing
		assertThrows( NoSuchElementException.class, () -> registry.create(lop, spo) );
	}

	public void assertSupportForOpBindJoinWithUNION( final ConstructorGPAddAndGPOptAdd ctor,
	                                                 final PhysicalOpRegistry registry ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final UnaryLogicalOp lop = ctor.create( new EngineTestBase.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );

		final UnaryPhysicalOp pop = registry.create(lop, sp);

		// assert type
		assertEquals( PhysicalOpBindJoinSPARQL.class, pop.getClass() );
		final PhysicalOpBindJoinSPARQL bj = (PhysicalOpBindJoinSPARQL) pop;
		assertEquals( "UNION_BASED", bj.getType() );
	}
}
