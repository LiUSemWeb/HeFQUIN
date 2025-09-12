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

public class TestForPhysicalOpFactory
{
	@Test
	public void testOpBindJoinWithBoundJoin_gpAdd(){
		final PhysicalOpFactory factory = new PhysicalOpFactory()
			.register( new PhysicalOpBindJoinWithBoundJoin.Provider() );
		assertSupportFor(LogicalOpGPAdd::new, factory);
	}

	@Test
	public void testOpBindJoinWithBoundJoin_gpOptAdd(){
		final PhysicalOpFactory factory = new PhysicalOpFactory()
			.register( new PhysicalOpBindJoinWithBoundJoin.Provider() );
		assertSupportFor(LogicalOpGPAdd::new, factory);
	}


	public void assertSupportFor( final LogicalOpConstructor logicalOpConstructor, final PhysicalOpFactory factory ){
		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );

		final ExpectedVariables sp = TestUtils.getExpectedVariables( List.of("s", "p"), List.of() );
		final ExpectedVariables spo = TestUtils.getExpectedVariables( List.of("s", "p", "o"), List.of() );

		// wrong type returned
		assertEquals( PhysicalOpBindJoinWithBoundJoin.class, factory.create(lop, sp).getClass());
		// non-joinable var missing
		assertThrows( UnsupportedOperationException.class, () -> factory.create(lop, spo) );
	}
}
