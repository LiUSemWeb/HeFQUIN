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
import org.apache.jena.sparql.expr.E_IsIRI;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.Element;
import org.apache.jena.sparql.syntax.ElementGroup;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Test;

import se.liu.ida.hefquin.base.query.ExpectedVariables;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.engine.queryplan.logical.LogicalOperator;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGPOptAdd;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpRightJoin;
import se.liu.ida.hefquin.engine.queryplan.logical.impl.LogicalOpUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBinaryUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBind;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithBoundJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithUNION;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUES;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpBindJoinWithVALUESorFILTER;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpFilter;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpGlobalToLocal;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpHashRJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpIndexNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpLocalToGlobal;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpMultiwayUnion;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpNaiveNestedLoopsJoin;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpRequest;
import se.liu.ida.hefquin.engine.queryplan.physical.impl.PhysicalOpSymmetricHashJoin;
import se.liu.ida.hefquin.federation.FederationMember;
import se.liu.ida.hefquin.federation.access.impl.req.SPARQLRequestImpl;

public class TestsForPhysicalOpFactories {
    interface LogicalOpConstructor extends BiFunction<FederationMember, SPARQLGraphPattern, LogicalOperator> {
        // first arg is the federation member the LogicalOperator constructors accept
        // second arg is the SPARQLGraphPattern the LogicalOperator constructors accept
    }

	@Test
	public void testPhysicalOpBinaryUnion() {
		final PhysicalOpFactory factory = PhysicalOpBinaryUnion.getFactory();

		final LogicalOperator lop_union = LogicalOpUnion.getInstance();

		assertEquals( PhysicalOpBinaryUnion.class, factory.create(lop_union).getClass() );
		assertTrue( factory.supports(lop_union, (ExpectedVariables) null));
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpBind() {
		final PhysicalOpFactory factory = PhysicalOpBind.getFactory();

		final Var v = Var.alloc("x");
		final Expr bindExpr = NodeValue.makeInteger(42);
		final VarExprList bindExpressions = new VarExprList(v, bindExpr);
		final LogicalOperator lop_bind = new LogicalOpBind(bindExpressions);

		assertEquals( PhysicalOpBind.class, factory.create(lop_bind).getClass() );
		assertTrue( factory.supports(lop_bind, (ExpectedVariables) null));
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpBindJoin() {
		assertSupportForOpBindJoin( LogicalOpGPAdd::new );
		assertSupportForOpBindJoin( LogicalOpGPOptAdd::new );
	}

	@Test
	public void testPhysicalOpBindJoinWithBoundJoin() {
		final PhysicalOpFactory factory = PhysicalOpBindJoinWithBoundJoin.getFactory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithBoundJoin.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithBoundJoin.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithFILTER() {
		final PhysicalOpFactory factory = PhysicalOpBindJoinWithFILTER.getFactory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithFILTER.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithFILTER.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithUNION() {
		final PhysicalOpFactory factory = PhysicalOpBindJoinWithUNION.getFactory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithUNION.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithUNION.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithVALUES() {
		final PhysicalOpFactory factory = PhysicalOpBindJoinWithVALUES.getFactory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithVALUES.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithVALUES.class, factory );
	}

	@Test
	public void testPhysicalOpBindJoinWithVALUESorFILTER() {
		final PhysicalOpFactory factory = PhysicalOpBindJoinWithVALUESorFILTER.getFactory();
		assertSupportForOpBindJoinWithX( LogicalOpGPAdd::new, PhysicalOpBindJoinWithVALUESorFILTER.class, factory );
		assertSupportForOpBindJoinWithX( LogicalOpGPOptAdd::new, PhysicalOpBindJoinWithVALUESorFILTER.class, factory );
	}

	@Test
	public void testPhysicalOpFilter() {
		final PhysicalOpFactory factory = PhysicalOpFilter.getFactory();

		final Var v = Var.alloc("x");
		final Expr e = new E_IsIRI( new ExprVar(v) );
		final LogicalOperator lop = new LogicalOpFilter(e);

		assertEquals( PhysicalOpFilter.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpGlobalToLocal() {
		final PhysicalOpFactory factory = PhysicalOpGlobalToLocal.getFactory();
		final LogicalOperator lop = new LogicalOpGlobalToLocal( TestUtils.getVocabularyMappingForTest() );

		assertEquals( PhysicalOpGlobalToLocal.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpHashJoin() {
		final PhysicalOpFactory factory = PhysicalOpHashJoin.getFactory();
		final LogicalOperator lop = LogicalOpJoin.getInstance();

		final ExpectedVariables vars1 = TestUtils.getExpectedVariables( List.of("x", "y"), List.of() );
		final ExpectedVariables vars2 = TestUtils.getExpectedVariables( List.of("x"),      List.of() );
		final ExpectedVariables vars3 = TestUtils.getExpectedVariables( List.of(),         List.of("x", "y") );
		final ExpectedVariables vars4 = TestUtils.getExpectedVariables( List.of(),         List.of("x") );
		final ExpectedVariables vars5 = TestUtils.getExpectedVariables( List.of(),         List.of() );

		assertTrue( factory.supports(lop, vars1, vars2) );  // overlap certain
		assertFalse( factory.supports(lop, vars1, vars3) ); // overlap certain/possible
		assertFalse( factory.supports(lop, vars3, vars4) ); // overlap possible/possible
		assertFalse( factory.supports(lop, vars1, vars5) ); // no overlap

		assertEquals( PhysicalOpHashJoin.class, factory.create(lop).getClass() );
		assertFalse( factory.supports( new LogicalOpGlobalToLocal(null), (ExpectedVariables) null ) );
	}

	@Test
	public void testPhysicalOpHashRJoin() {
		final PhysicalOpFactory factory = PhysicalOpHashRJoin.getFactory();
		final LogicalOperator lop = LogicalOpRightJoin.getInstance();

		assertEquals( PhysicalOpHashRJoin.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpIndexNestedLoopsJoin() {
		final PhysicalOpFactory factory = PhysicalOpIndexNestedLoopsJoin.getFactory();
		assertSupportForOpIndexNestedLoopsJoin( LogicalOpGPAdd::new, PhysicalOpIndexNestedLoopsJoin.class, factory );
		assertSupportForOpIndexNestedLoopsJoin( LogicalOpGPOptAdd::new, PhysicalOpIndexNestedLoopsJoin.class, factory );
	}

	@Test
	public void testPhysicalOpLocalToGlobal() {
		final PhysicalOpFactory factory = PhysicalOpLocalToGlobal.getFactory();
		final LogicalOperator lop = new LogicalOpLocalToGlobal( TestUtils.getVocabularyMappingForTest() );

		assertEquals( PhysicalOpLocalToGlobal.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpMultiwayUnion() {
		final PhysicalOpFactory factory = PhysicalOpMultiwayUnion.getFactory();
		final LogicalOperator lop = LogicalOpMultiwayUnion.getInstance();

		assertEquals( PhysicalOpMultiwayUnion.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpJoin.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpNaiveNestedLoopsJoin() {
		final PhysicalOpFactory factory = PhysicalOpNaiveNestedLoopsJoin.getFactory();
		final LogicalOperator lop = LogicalOpJoin.getInstance();

		assertEquals( PhysicalOpNaiveNestedLoopsJoin.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpUnion.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpParallelMultiLeftJoin() {
		// TODO: Making the factory for PhysicalOpParallelMultiLeftJoin is not
		// straightforward. It already contains a method checkApplicability(...), which
		// can be viewed as equivalent to the PhysicalOpFactory#supported; however,
		// checkApplicability(...) also requires access to all PhysicalPlan[] children...
		// Should we treat this a special case?
	}

	@Test
	public void testPhysicalOpRequest() {
		final PhysicalOpFactory factory = PhysicalOpRequest.getFactory();

		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final TriplePattern tp = new TriplePatternImpl(v1, v2, v3);
		final FederationMember fm = new TestUtils.SPARQLEndpointForTest();
		final LogicalOpRequest<?,?> lop = new LogicalOpRequest<>( fm, new SPARQLRequestImpl(tp) );

		assertEquals( PhysicalOpRequest.class, factory.create(lop).getClass() );
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpUnion.getInstance(), (ExpectedVariables) null) );
	}

	@Test
	public void testPhysicalOpSymmetricHashJoin() {
		final PhysicalOpFactory factory = PhysicalOpSymmetricHashJoin.getFactory();
		final LogicalOpJoin lop = LogicalOpJoin.getInstance();

		final ExpectedVariables vars1 = TestUtils.getExpectedVariables( List.of("x", "y"), List.of() );
		final ExpectedVariables vars2 = TestUtils.getExpectedVariables( List.of("x"),      List.of() );
		final ExpectedVariables vars3 = TestUtils.getExpectedVariables( List.of(),         List.of("x", "y") );
		final ExpectedVariables vars4 = TestUtils.getExpectedVariables( List.of(),         List.of("x") );
		final ExpectedVariables vars5 = TestUtils.getExpectedVariables( List.of(),         List.of() );

		assertTrue( factory.supports(lop, vars1, vars2) );  // overlap certain
		assertFalse( factory.supports(lop, vars1, vars3) ); // overlap certain/possible
		assertFalse( factory.supports(lop, vars3, vars4) ); // overlap possible/possible
		assertFalse( factory.supports(lop, vars1, vars5) ); // no overlap

		assertEquals( PhysicalOpSymmetricHashJoin.class, factory.create(lop).getClass() );
		assertFalse( factory.supports( new LogicalOpGlobalToLocal(null), (ExpectedVariables) null ) );
	}

	// ---- helper functions -----

	public void assertSupportForOpBindJoin( final LogicalOpConstructor logicalOpConstructor ) {
		final PhysicalOpFactory factory = PhysicalOpBindJoin.getFactory();

		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(v1, v2, v3));

		final SPARQLGraphPattern p1 = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop_sparql = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p1 );
		final LogicalOperator lop_tpf = logicalOpConstructor.apply( new TestUtils.TPFServerForTest(), p1 );
		final LogicalOperator lop_brtpf = logicalOpConstructor.apply( new TestUtils.BRTPFServerForTest(), p1 );

		assertEquals( PhysicalOpBindJoin.class, factory.create(lop_sparql).getClass() );

		assertFalse( factory.supports( lop_sparql, (ExpectedVariables) null ) );
		assertFalse( factory.supports( lop_tpf, (ExpectedVariables) null ) );
		assertTrue( factory.supports( lop_brtpf, (ExpectedVariables) null ) );

		// all other pattern types should fail
		final ElementGroup el2 = new ElementGroup();
		final SPARQLGraphPattern p2 = new GenericSPARQLGraphPatternImpl1(el2);
		final LogicalOperator lop_unsupported = logicalOpConstructor.apply( new TestUtils.BRTPFServerForTest(), p2 );
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
		// null value for exepcted variables
		assertTrue( factory.supports(lop, (ExpectedVariables) null) );

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

	public void assertSupportForOpIndexNestedLoopsJoin( final LogicalOpConstructor logicalOpConstructor,
	                                                    final Class<? extends PhysicalOperator> opClass,
	                                                    final PhysicalOpFactory factory ){

		final String queryString = "SELECT * WHERE { ?s ?p ?o }";
		final Element el = QueryFactory.create(queryString).getQueryPattern();
		final SPARQLGraphPattern p = new GenericSPARQLGraphPatternImpl1(el);
		final LogicalOperator lop_sparql = logicalOpConstructor.apply( new TestUtils.SPARQLEndpointForTest(), p );
		final LogicalOperator lop_tpf = logicalOpConstructor.apply( new TestUtils.TPFServerForTest(), p );
		final LogicalOperator lop_brtpf = logicalOpConstructor.apply( new TestUtils.BRTPFServerForTest(), p );

		assertEquals( opClass, factory.create(lop_sparql).getClass() );
		assertEquals( opClass, factory.create(lop_tpf).getClass() );
		assertEquals( opClass, factory.create(lop_brtpf).getClass() );

		assertTrue( factory.supports(lop_sparql, (ExpectedVariables) null) );
		assertTrue( factory.supports(lop_tpf, (ExpectedVariables) null) );
		assertTrue( factory.supports(lop_brtpf, (ExpectedVariables) null) );
		assertFalse( factory.supports(LogicalOpUnion.getInstance(), (ExpectedVariables) null) );
	}
}
