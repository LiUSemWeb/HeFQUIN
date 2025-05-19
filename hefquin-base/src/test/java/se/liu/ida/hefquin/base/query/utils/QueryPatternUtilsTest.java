package se.liu.ida.hefquin.base.query.utils;

import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.Op;
import org.apache.jena.sparql.algebra.op.*;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.core.VarExprList;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.*;
import org.junit.Test;
import static org.junit.Assert.*;

import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;

import java.util.List;

/**
 * Unit tests for the QueryPatternUtils class. Verifies correct translation of
 * SPARQLGraphPatterns into Jena Op and Element structures.
 */
public class QueryPatternUtilsTest {
	/** Tests conversion of a single TriplePattern to an OpTriple. */
	@Test
	public void testTriplePatternToOp() {
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		final Op op = QueryPatternUtils.convertToJenaOp(tp);

		assertTrue( op instanceof OpTriple );
		assertEquals( tp.asJenaTriple(), ((OpTriple) op).getTriple() );
	}

	/** Tests conversion of a BGP to an OpBGP. */
	@Test
	public void testBGPToOp() {
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final TriplePattern tp3 = new TriplePatternImpl(v3, v3, v3);

		final BGP bgp = new BGPImpl(tp1, tp2, tp3);
		final Op op = QueryPatternUtils.convertToJenaOp(bgp);
		assertTrue( op instanceof OpBGP );

		final OpBGP opBGP = (OpBGP) op;
		final List<Triple> opTriples = opBGP.getPattern().getList();
		assertTrue( opTriples.contains( tp1.asJenaTriple() ) );
		assertTrue( opTriples.contains( tp2.asJenaTriple() ) );
		assertTrue( opTriples.contains( tp3.asJenaTriple() ) );
	}

	/** Tests conversion of a SPARQLUnionPattern to an OpUnion. */
	@Test
	public void testUnionToOp() {
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
		union.addSubPattern(tp1);
		union.addSubPattern(tp2);

		final Op op = QueryPatternUtils.convertToJenaOp(union);
		assertTrue( op instanceof OpUnion );

		final OpUnion opUnion = (OpUnion) op;
		final Op left = opUnion.getLeft();
		final Op right = opUnion.getRight();
		assertTrue( left instanceof OpTriple );
		assertTrue( right instanceof OpTriple );

		assertEquals( tp1.asJenaTriple(), ((OpTriple) left).getTriple() );
		assertEquals( tp2.asJenaTriple(), ((OpTriple) right).getTriple() );
    }

	/** Tests conversion of a SPARQLGroupPattern to an OpJoin. */
	@Test
	public void testGroupToOp() {
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);

		final SPARQLGroupPatternImpl group = new SPARQLGroupPatternImpl();
		group.addSubPattern(tp1);
		group.addSubPattern(tp2);

		final Op op = QueryPatternUtils.convertToJenaOp(group);
		assertTrue(op instanceof OpJoin);

		final OpJoin join = (OpJoin) op;
		final Op left = join.getLeft();
		final Op right = join.getRight();

		assertTrue( left instanceof OpTriple );
		assertTrue( right instanceof OpTriple );

		assertEquals( tp1.asJenaTriple(), ((OpTriple) left).getTriple() );
		assertEquals( tp2.asJenaTriple(), ((OpTriple) right).getTriple() );
	}

	/**
	 * Tests conversion of a GenericSPARQLGraphPatternImpl1 (ElementGroup-based) to
	 * OpBGP.
	 */
	@Test
	public void testGenericSPARQLGraphPatternImpl1ToOp() {
		// Tests GenericSPARQLGraphPatternImpl1
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final TriplePattern tp3 = new TriplePatternImpl(v3, v3, v3);

		final ElementGroup el = new ElementGroup();
		el.addTriplePattern( tp1.asJenaTriple() );
		el.addTriplePattern( tp2.asJenaTriple() );
		el.addTriplePattern( tp3.asJenaTriple() );
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1( el );

		final Op op = QueryPatternUtils.convertToJenaOp(pattern);
		assertTrue( op instanceof OpBGP );

		final OpBGP opBGP = (OpBGP) op;
		final List<Triple> opTriples = opBGP.getPattern().getList();
		assertEquals( 3, opTriples.size() );
		assertTrue( opTriples.contains( tp1.asJenaTriple() ) );
		assertTrue( opTriples.contains( tp2.asJenaTriple() ) );
		assertTrue( opTriples.contains( tp3.asJenaTriple() ) );
	}

	/**
	 * Tests conversion of a GenericSPARQLGraphPatternImpl2 (Op-based) to OpExtend.
	 */
	@Test
	public void testGenericSPARQLGraphPatternImpl2ToOp() {
		// Tests GenericSPARQLGraphPatternImpl2
		final Var v = Var.alloc("x");
		final Expr expr = NodeValue.makeInteger(42);
		
		final Op originalOp = makeOpExtend(v, expr);
		final SPARQLGraphPattern sparqlGraphPattern = new GenericSPARQLGraphPatternImpl2(originalOp);
		System.err.println(sparqlGraphPattern.getClass());
		final Op op = QueryPatternUtils.convertToJenaOp(sparqlGraphPattern);
		assertTrue( op instanceof OpExtend );

		final OpExtend opExtend = (OpExtend) op;
		final VarExprList varExprList = opExtend.getVarExprList();
		assertEquals( v, varExprList.getVars().get(0) );
		assertEquals( expr, varExprList.getExpr(v) );
	}

	/** Tests conversion of a single TriplePattern to an ElementTriplesBlock. */
	@Test
	public void testTriplePatternToElement() {
		final Var v1 = Var.alloc("x");
		final TriplePattern tp = new TriplePatternImpl(v1, v1, v1);
		
		final Element el = QueryPatternUtils.convertToJenaElement(tp);
		assertTrue( el instanceof ElementTriplesBlock );

		final ElementTriplesBlock etb = (ElementTriplesBlock) el;
		final List<Triple> list = etb.getPattern().getList();
		assertEquals( 1, list.size() );
		assertTrue( list.contains( tp.asJenaTriple() ) );
	}

	/** Tests conversion of a BGP to an ElementTriplesBlock. */
	@Test
	public void testBGPToElement() {
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final TriplePattern tp3 = new TriplePatternImpl(v3, v3, v3);

		final BGP bgp = new BGPImpl(tp1, tp2, tp3);
		final Element el = QueryPatternUtils.convertToJenaElement(bgp);
		assertTrue( el instanceof ElementTriplesBlock );
		
		final ElementTriplesBlock etb = (ElementTriplesBlock) el;
		final List<Triple> list = etb.getPattern().getList();
		assertEquals( 3, list.size() );
		assertTrue( list.contains( tp1.asJenaTriple() ) );
		assertTrue( list.contains( tp2.asJenaTriple() ) );
		assertTrue( list.contains( tp3.asJenaTriple() ) );
	}

	/** Tests conversion of a SPARQLUnionPattern to an ElementUnion. */
	@Test
	public void testUnionToElement() {
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final SPARQLUnionPatternImpl union = new SPARQLUnionPatternImpl();
		union.addSubPattern(tp1);
		union.addSubPattern(tp2);

		final Element el = QueryPatternUtils.convertToJenaElement(union);
		assertTrue( el instanceof ElementUnion );
		
		final List<Element> list = ((ElementUnion) el).getElements();
		assertEquals( 2, list.size() );

		final Element left = list.get(0);
		final Element right = list.get(1);

		assertTrue( left instanceof ElementTriplesBlock );
		assertTrue( right instanceof ElementTriplesBlock );

		final ElementTriplesBlock etb1 = (ElementTriplesBlock) left;
		final List<Triple> list1 = etb1.getPattern().getList();
		assertEquals( 1, list1.size() );
		assertTrue( list1.contains( tp1.asJenaTriple() ) );
		
		final ElementTriplesBlock etb2 = (ElementTriplesBlock) right;
		final List<Triple> list2 = etb2.getPattern().getList();
		assertTrue( list2.contains( tp2.asJenaTriple() ) );
	}

	/** Tests conversion of a SPARQLGroupPattern to an ElementGroup. */
	@Test
	public void testGroupToElement() {
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);

		final SPARQLGroupPatternImpl group = new SPARQLGroupPatternImpl();
		group.addSubPattern(tp1);
		group.addSubPattern(tp2);

		final Element el = QueryPatternUtils.convertToJenaElement(group);
		assertTrue(el instanceof ElementGroup);

		final List<Element> list = ((ElementGroup) el).getElements();
		final Element left = list.get(0);
		final Element right = list.get(1);

		assertTrue( left instanceof ElementTriplesBlock );
		assertTrue( right instanceof ElementTriplesBlock );

		final ElementTriplesBlock etb1 = (ElementTriplesBlock) left;
		final List<Triple> list1 = etb1.getPattern().getList();
		assertEquals( 1, list1.size() );
		assertTrue( list1.contains( tp1.asJenaTriple() ) );
		
		final ElementTriplesBlock etb2 = (ElementTriplesBlock) right;
		final List<Triple> list2 = etb2.getPattern().getList();
		assertEquals( 1, list2.size() );
		assertTrue( list2.contains( tp2.asJenaTriple() ) );
	}

	/**
	 * Tests conversion of a GenericSPARQLGraphPatternImpl1 (ElementGroup-based) to
	 * ElementGroup.
	 */
	@Test
	public void testGenericSPARQLGraphPatternImpl1ToElement() {
		// Tests GenericSPARQLGraphPatternImpl1
		final Var v1 = Var.alloc("x");
		final Var v2 = Var.alloc("y");
		final Var v3 = Var.alloc("z");

		final TriplePattern tp1 = new TriplePatternImpl(v1, v1, v1);
		final TriplePattern tp2 = new TriplePatternImpl(v2, v2, v2);
		final TriplePattern tp3 = new TriplePatternImpl(v3, v3, v3);

		final ElementGroup elOriginal = new ElementGroup();
		elOriginal.addTriplePattern( tp1.asJenaTriple() );
		elOriginal.addTriplePattern( tp2.asJenaTriple() );
		elOriginal.addTriplePattern( tp3.asJenaTriple() );
		final SPARQLGraphPattern pattern = new GenericSPARQLGraphPatternImpl1( elOriginal );

		final Element el = QueryPatternUtils.convertToJenaElement(pattern);
		assertTrue( el instanceof ElementGroup );
		
		final ElementGroup elGroup = (ElementGroup) el;
		assertTrue( elGroup.get(0) instanceof ElementTriplesBlock );

		final ElementTriplesBlock etb = (ElementTriplesBlock) elGroup.get(0);
		final List<Triple> list = etb.getPattern().getList();
		assertEquals( 3, list.size() );
		assertTrue( list.contains( tp1.asJenaTriple() ) );
		assertTrue( list.contains( tp2.asJenaTriple() ) );
		assertTrue( list.contains( tp3.asJenaTriple() ) );
	}

	/**
	 * Tests conversion of a GenericSPARQLGraphPatternImpl2 (OpExtend) to an
	 * ElementGroup with ElementBind.
	 */
	@Test
	public void testGenericSPARQLGraphPatternImpl2ToElement() {
		final Var v = Var.alloc("x");
		final Expr expr = NodeValue.makeInteger(42);

		final Op originalOp = makeOpExtend(v, expr);
		final SPARQLGraphPattern sparqlGraphPattern = new GenericSPARQLGraphPatternImpl2(originalOp);
		
		final Element el = QueryPatternUtils.convertToJenaElement(sparqlGraphPattern);
		System.err.println(el.getClass());
		assertTrue( el instanceof ElementGroup );
		
		final ElementGroup  elGroup = (ElementGroup) el;
		assertTrue( elGroup.get(0) instanceof ElementBind );

		final ElementBind elBind = (ElementBind) elGroup.get(0);
		assertEquals( v, elBind.getVar() );
		assertEquals( expr, elBind.getExpr() );
	}

	/** Helper method to create an OpExtend with a given variable and expression. */
	public Op makeOpExtend( final Var v, final Expr expr ){
		final Op base = OpTable.unit();
		final VarExprList varExprList = new VarExprList();
		varExprList.add(v, expr);
		return OpExtend.create(base, v, expr);
	}
}