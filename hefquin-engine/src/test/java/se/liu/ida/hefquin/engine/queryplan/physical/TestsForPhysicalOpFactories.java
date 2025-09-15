package se.liu.ida.hefquin.engine.queryplan.physical;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.function.BiFunction;

import org.apache.jena.graph.Triple;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUESorFILTER;
import se.liu.ida.hefquin.federation.FederationMember;

public class TestsForPhysicalOpFactories {
    interface LogicalOpConstructor extends BiFunction<FederationMember, SPARQLGraphPattern, LogicalOperator> {
        // first arg is the federation member type your constructors accept
    }
	// Complete:
	// - PhysicalOpBinaryUnion.java
	// - PhysicalOpBind.java
	// - PhysicalOpBindJoin.java
	// - PhysicalOpBindJoinWithBoundJoin.java
	// - PhysicalOpBindJoinWithFILTER.java
	// - PhysicalOpBindJoinWithUNION.java
	// - PhysicalOpBindJoinWithVALUES.java
	// - PhysicalOpBindJoinWithVALUESorFILTER.java
	// TODO:
	// - PhysicalOpFilter.java
	// - PhysicalOpGlobalToLocal.java
	// - PhysicalOpHashJoin.java
	// - PhysicalOpHashRJoin.java
	// - PhysicalOpIndexNestedLoopsJoin.java
	// - PhysicalOpLocalToGlobal.java
	// - PhysicalOpMultiwayUnion.java
	// - PhysicalOpNaiveNestedLoopsJoin.java
	// - PhysicalOpParallelMultiLeftJoin.java
	// - PhysicalOpRequest.java
	// - PhysicalOpSymmetricHashJoin.java
	// - PhysicalPlanWithBinaryRootImpl.java
	// - PhysicalPlanWithNaryRootImpl.java
	// - PhysicalPlanWithNullaryRootImpl.java
	// - PhysicalPlanWithUnaryRootImpl.java

	@Test
	public void testPhysicalOpBinaryUnion() {
		final PhysicalOpFactory factory = new PhysicalOpBinaryUnion.Factory();

		final LogicalOperator lop_union = LogicalOpUnion.getInstance();
		final LogicalOperator lop_join = LogicalOpJoin.getInstance();

		assertTrue(  factory.supports(lop_union, (ExpectedVariables) null));
		assertFalse( factory.supports(lop_join, (ExpectedVariables) null));
	}

	@Test
	public void testPhysicalOpBind() {
		final PhysicalOpFactory factory = new PhysicalOpBind.Factory();

		final Var v = Var.alloc("x");
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v, bindExpr);
		final LogicalOperator lop_bind = new LogicalOpBind(bindExpressions);
		final LogicalOperator lop_join = LogicalOpJoin.getInstance();

		assertTrue(  factory.supports(lop_bind, (ExpectedVariables) null));
		assertFalse( factory.supports(lop_join, (ExpectedVariables) null));
	}

	@Test
	public void testPhysicalOpBindJoin() {
		assertSupportForOpBindJoin( LogicalOpGPAdd::new );
		assertSupportForOpBindJoin( LogicalOpGPOptAdd::new );
	}

	@Test
	public void testPhysicalOpBindJoinWithBoundJoin() {
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithBoundJoin.Factory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithBoundJoin.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithBoundJoin.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithFILTER() {
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithFILTER.Factory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithFILTER.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithFILTER.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithUNION() {
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithUNION.Factory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithUNION.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithUNION.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithVALUES() {
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithVALUES.Factory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithVALUES.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithVALUES.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithVALUESorFILTER() {
		final PhysicalOpFactory factory = new PhysicalOpBindJoinWithVALUESorFILTER.Factory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithVALUESorFILTER.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithVALUESorFILTER.class, factory );
	}

	// ---- helper functions -----

	public void assertSupportForOpBindJoin( final LogicalOpConstructor logicalOpConstructor ) {
		final PhysicalOpFactory factory = new PhysicalOpBindJoin.Factory();

		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(v1, v2, v3));

		final SPARQLGraphPattern p1 = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop_sparql = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p1 );
		final LogicalOperator lop_tpf = logicalOpConstructor.apply( new TestUtils.TPFServerForTest(), p1 );
		final LogicalOperator lop_brtpf = logicalOpConstructor.apply( new TestUtils.BRTPFServerForTest(), p1 );

		assertTrue( factory.supports( lop_sparql, (ExpectedVariables) null ) );
		assertTrue( factory.supports( lop_tpf, (ExpectedVariables) null ) );
		assertTrue( factory.supports( lop_brtpf, (ExpectedVariables) null ) );

		// all other pattern types should fail
		final ElementGroup el2 = new ElementGroup();
		final SPARQLGraphPattern p2 = new GenericSPARQLGraphPatternImpl1(el2);
		final LogicalOperator lop_unsupported = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p2 );
		assertFalse( factory.supports( lop_unsupported, (ExpectedVariables) null ) );
	}

	public void assertSupportForOpBindJoinWithX( final LogicalOpConstructor logicalOpConstructor,
	                                             final Class<? extends PhysicalOperator> opClass,
	                                             final PhysicalOpFactory factory ){

		final String queryString = "SELECT * WHERE { ?s ?p ?o OPTIONAL { ?_s ?_p ?o} }";
		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );
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

		assertEquals( opClass, factory.create(lop).getClass() );

		// certain vars
		assertTrue( factory.supports(lop, sp1) );
		assertTrue( factory.supports(lop, po1) );
		assertTrue( factory.supports(lop, so1) );
		// possible vars
		assertTrue( factory.supports(lop, sp2) );
		assertTrue( factory.supports(lop, po2) );
		assertTrue( factory.supports(lop, so2) );

		// supports cases with no non-joining vars?
		if ( opClass.equals(PhysicalOpBindJoinWithBoundJoin.class) ) {
			assertFalse( factory.supports(lop, spo1) );
			assertFalse( factory.supports(lop, spo2) );
		}
		else {
			assertTrue( factory.supports(lop, spo1) );
			assertTrue( factory.supports(lop, spo2) );
		}

		// unsupported federation member types
		assertFalse( factory.supports(lop_tpf, (ExpectedVariables) null ) );
		assertFalse( factory.supports(lop_brtpf,  (ExpectedVariables) null ) );
	}
}
