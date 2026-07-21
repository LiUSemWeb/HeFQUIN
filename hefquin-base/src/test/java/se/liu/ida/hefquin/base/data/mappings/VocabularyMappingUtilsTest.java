package se.liu.ida.hefquin.base.data.mappings;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.*;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.graph.Triple;
import org.apache.jena.sparql.algebra.op.OpBGP;
import org.apache.jena.sparql.core.BasicPattern;
import org.apache.jena.sparql.expr.E_Equals;
import org.apache.jena.sparql.expr.E_LogicalAnd;
import org.apache.jena.sparql.expr.E_LogicalOr;
import org.apache.jena.sparql.expr.E_NotEquals;
import org.apache.jena.sparql.expr.E_Str;
import org.apache.jena.sparql.expr.E_StrContains;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprList;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.apache.jena.sparql.syntax.ElementTriplesBlock;
import org.junit.Before;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.VocabularyMapping;
import se.liu.ida.hefquin.base.query.BGP;
import se.liu.ida.hefquin.base.query.SPARQLGraphPattern;
import se.liu.ida.hefquin.base.query.SPARQLGroupPattern;
import se.liu.ida.hefquin.base.query.SPARQLUnionPattern;
import se.liu.ida.hefquin.base.query.TriplePattern;
import se.liu.ida.hefquin.base.query.impl.BGPImpl;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl1;
import se.liu.ida.hefquin.base.query.impl.GenericSPARQLGraphPatternImpl2;
import se.liu.ida.hefquin.base.query.impl.SPARQLGroupPatternImpl;
import se.liu.ida.hefquin.base.query.impl.SPARQLUnionPatternImpl;
import se.liu.ida.hefquin.base.query.impl.TriplePatternImpl;
import se.liu.ida.hefquin.testutils.TestUtils;

public class VocabularyMappingUtilsTest
{
	// Entities (for entity mappings)
	final Node e    = NodeFactory.createURI("http://example.org/e");
	final Node e_g  = NodeFactory.createURI("http://example.org/global/e");
	final Node e_l1 = NodeFactory.createURI("http://example.org/local/e1");
	final Node e_l2 = NodeFactory.createURI("http://example.org/local/e2");
	// Classes (used as subjects in schema mappings)
	final Node s    = NodeFactory.createURI("http://example.org/s");
	final Node s_g  = NodeFactory.createURI("http://example.org/global/s");
	final Node s_l1 = NodeFactory.createURI("http://example.org/local/s1");
	final Node s_l2 = NodeFactory.createURI("http://example.org/local/s2");
	// Predicates (properties, schema mappings)
	final Node p    = NodeFactory.createURI("http://example.org/p");
	final Node p_g  = NodeFactory.createURI("http://example.org/global/p");
	final Node p_l1 = NodeFactory.createURI("http://example.org/local/p1");
	final Node p_l2 = NodeFactory.createURI("http://example.org/local/p2");
	// Classes (used as objects in schema mappings)
	final Node o    = NodeFactory.createURI("http://example.org/o");
	final Node o_g  = NodeFactory.createURI("http://example.org/global/o");
	final Node o_l1 = NodeFactory.createURI("http://example.org/local/o1");
	final Node o_l2 = NodeFactory.createURI("http://example.org/local/o2");

	// Vocabulary mapping
	VocabularyMapping vm;

	@Before
	public void setup() {
		vm = TestUtils.createVocabularyMapping();
	}

	// TriplePattern
	@Test
	public void tp_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	@Test
	public void tp_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	@Test
	public void tp_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	@Test
	public void tp_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3);
		assertMappingTranslation(tp1, expected);
	}

	// BGP
	@Test
	public void bgp_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp4 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new BGPImpl(tp1, tp2), expected );
	}

	@Test
	public void bgp_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new BGPImpl(tp1, tp2), expected );
	}

	@Test
	public void bgp_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new BGPImpl(tp1, tp2), expected );
	}

	@Test
	public void bgp_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp4 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation(  new BGPImpl(tp1, tp2), expected );
	}

	// SPARQLGroupPattern
	@Test
	public void group_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp4 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void group_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void group_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void group_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp4 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation(  new SPARQLGroupPatternImpl(tp1, tp2), expected );
	}

	// SPARQLUnionPattern
	@Test
	public void union_subject_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(e_g, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp4 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void union_object_entity() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void union_object_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o_g);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp4 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation( new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	@Test
	public void union_predicate_schema() {
		final TriplePattern tp1 = new TriplePatternImpl(s, p_g, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp4 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp2, tp3, tp4);
		assertMappingTranslation(  new SPARQLUnionPatternImpl(tp1, tp2), expected );
	}

	// GenericSPARQLGraphPatternImpl1
	@Test
	public void generic1_subject_entity() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(e_g, p, o));
        el.addTriple(Triple.create(e, p, o));
		final TriplePattern tp1 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	@Test
	public void generic1_object_entity() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(s, p, e_g));
        el.addTriple(Triple.create(s, p, e));
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	@Test
	public void generic1_object_schema() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(s, p, o_g));
        el.addTriple(Triple.create(s, p, o));
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	@Test
	public void generic1_predicate_schema() {
		final ElementTriplesBlock el = new ElementTriplesBlock();
        el.addTriple(Triple.create(s, p_g, o));
        el.addTriple(Triple.create(s, p, o));
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl1(el), expected );
	}

	// GenericSPARQLGraphPatternImpl2
	@Test
	public void generic2_subject_entity() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(e_g, p, o));
        bp.add(Triple.create(e, p, o));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(e, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(e_l1, p, o);
		final TriplePattern tp3 = new TriplePatternImpl(e_l2, p, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void generic2_object_entity() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(s, p, e_g));
        bp.add(Triple.create(s, p, e));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(s, p, e);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, e_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, e_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void generic2_object_schema() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(s, p, o_g));
        bp.add(Triple.create(s, p, o));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p, o_l1);
		final TriplePattern tp3 = new TriplePatternImpl(s, p, o_l2);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void generic2_predicate_schema() {
		final BasicPattern bp = new BasicPattern();
        bp.add(Triple.create(s, p_g, o));
        bp.add(Triple.create(s, p, o));
		final OpBGP op = new OpBGP(bp);
		final TriplePattern tp1 = new TriplePatternImpl(s, p, o);
		final TriplePattern tp2 = new TriplePatternImpl(s, p_l1, o);
		final TriplePattern tp3 = new TriplePatternImpl(s, p_l2, o);
		final Set<TriplePattern> expected = Set.of(tp1, tp2, tp3);
		assertMappingTranslation( new GenericSPARQLGraphPatternImpl2(op), expected );
	}

	@Test
	public void translate_filter_equals_expression() {
		// Equality expressions are expanded into a logical OR over all mapped terms.
		// Set up
		final Node x = NodeFactory.createVariable("x");

		final Expr expr =
			new E_Equals(
				new ExprVar(x),
				NodeValue.makeNode(e_g)
			);

		final ExprList exprList = new ExprList(expr);

		// Test
		final ExprList result = VocabularyMappingUtils.translateExpressionsFromGlobal(exprList, vm);

		// Check
		assertTrue( result.get(0) instanceof E_LogicalOr );

		final E_LogicalOr or = (E_LogicalOr) result.get(0);

		assertTrue( or.getArg1() instanceof E_Equals );
		assertTrue( or.getArg2() instanceof E_Equals );

		final Node leftNode =
			( (NodeValue) ( (E_Equals) or.getArg1() ).getArg2() ).asNode();
		final Node rightNode =
			( (NodeValue) ( (E_Equals) or.getArg2() ).getArg2() ).asNode();

		assertEquals( Set.of(e_l1, e_l2),
					  Set.of(leftNode, rightNode) );
	}

	@Test
	public void translate_filter_or_expression() {
		// Rewritable logical expressions are recursively rewritten.
		// Set up
		final Node x = NodeFactory.createVariable("x");
		final Node y = NodeFactory.createVariable("y");

		final Expr expr =
			new E_Equals(
				new ExprVar(x),
				NodeValue.makeNode(e_g)
			);

		final Expr expr1 =
			new E_NotEquals(
				new ExprVar(y),
				NodeValue.makeNode(e_g)
			);

		final ExprList exprList = new ExprList( new E_LogicalOr(expr, expr1) );

		// Test
		final ExprList result = VocabularyMappingUtils.translateExpressionsFromGlobal(exprList, vm);

		// Check
		assertTrue( result.get(0) instanceof E_LogicalOr );
		final E_LogicalOr or = (E_LogicalOr) result.get(0);

		// Check first child and its children
		assertTrue( or.getArg1() instanceof E_LogicalOr );
		final E_LogicalOr orChild1 = (E_LogicalOr) or.getArg1();

		assertTrue( orChild1.getArg1() instanceof E_Equals );
		assertTrue( orChild1.getArg2() instanceof E_Equals );

		final Node orChild1LeftNode =
			( (NodeValue) ( (E_Equals) orChild1.getArg1() ).getArg2() ).asNode();
		final Node orChild1RightNode =
			( (NodeValue) ( (E_Equals) orChild1.getArg2() ).getArg2() ).asNode();

		assertEquals( Set.of(e_l1, e_l2),
					  Set.of(orChild1LeftNode, orChild1RightNode) );

		// Check second child and its children
		assertTrue( or.getArg2() instanceof E_LogicalAnd );
		final E_LogicalAnd orChild2 = (E_LogicalAnd) or.getArg2();

		assertTrue( orChild2.getArg1() instanceof E_NotEquals );
		assertTrue( orChild2.getArg2() instanceof E_NotEquals );

		final Node orChild2LeftNode =
			( (NodeValue) ( (E_NotEquals) orChild2.getArg1() ).getArg2() ).asNode();
		final Node orChild2RightNode =
			( (NodeValue) ( (E_NotEquals) orChild2.getArg2() ).getArg2() ).asNode();

		assertEquals( Set.of(e_l1, e_l2),
					  Set.of(orChild2LeftNode, orChild2RightNode) );
	}

	@Test
	public void translate_nonrewritable_filter_expression() {
		// Non-rewritable expressions should cause translation to fail.
		// Set up
		final Node x = NodeFactory.createVariable("x");

		final Expr notAllowedExpr =
			new E_StrContains(
				new ExprVar(x),
				NodeValue.makeLangString("test", "en")
			);

		final ExprList exprList = new ExprList(notAllowedExpr);

		// Test & Check
		assertThrows( UnsupportedOperationException.class, () -> VocabularyMappingUtils.translateExpressionsFromGlobal(exprList, vm) );
	}

	@Test
	public void translate_nonrewritable_filter_or_expression() {
		// Logical expressions containing a non-rewritable child should cause translation to fail.
		// Set up
		final Node x = NodeFactory.createVariable("x");

		final Expr expr =
			new E_Equals(
				new ExprVar(x),
				NodeValue.makeNode(e_g)
			);

		final Expr notAllowedExpr =
			new E_StrContains(
				new ExprVar(x),
				NodeValue.makeLangString("test", "en")
			);

		final ExprList exprList = new ExprList( new E_LogicalOr(expr, notAllowedExpr) );

		// Test & check
		assertThrows( UnsupportedOperationException.class, () -> VocabularyMappingUtils.translateExpressionsFromGlobal(exprList, vm) );
	}

	@Test
	public void translate_filter_twosided_equals_expression() {
		// Equality expressions with mapped terms on both sides are expanded into
		// the Cartesian product of all translated terms, combined with logical OR.

		// Set up
		final Expr expr =
			new E_Equals(
				NodeValue.makeNode(s_g),
				NodeValue.makeNode(e_g)
			);

		final ExprList exprList = new ExprList(expr);

		// Test
		final ExprList result = VocabularyMappingUtils.translateExpressionsFromGlobal(exprList, vm);

		// Check
		final Set<String> actual = TestUtils.extractEqualsPairs(result.get(0));

		// Order doesn't matter here
		assertEquals( Set.of(
			"<http://example.org/local/s1>=<http://example.org/local/e1>",
			"<http://example.org/local/s1>=<http://example.org/local/e2>",
			"<http://example.org/local/s2>=<http://example.org/local/e1>",
			"<http://example.org/local/s2>=<http://example.org/local/e2>"
		), actual );
	}

	@Test
	public void translate_nonrewritable_expression_with_mapped_uri() {
		// Non-rewritable expressions containing mapped global URIs are rejected.

		// Set up
		final Expr expr =
			new E_Str(
				NodeValue.makeNode(e_g)
			);

		// Test & check
		assertThrows( UnsupportedOperationException.class, () -> vm.translateExpressionFromLocal(expr) );
	}

	@Test
	public void translate_filter_expression_l2g() {
		// A filter expression containing a local term is rewritten to use the
		// corresponding global term.

		// Set up
		final Node x = NodeFactory.createVariable("x");

		final Expr expr =
			new E_Equals(
				new ExprVar(x),
				NodeValue.makeNode(e_l1)
			);

		final ExprList exprList = new ExprList(expr);

		// Test
		final ExprList result = VocabularyMappingUtils.translateExpressions(exprList, vm);

		// Check
		assertTrue( result.get(0) instanceof E_Equals );

		final E_Equals equals = (E_Equals) result.get(0);

		final Node node =
			( (NodeValue) equals.getArg2() ).asNode();

		assertEquals( Set.of(e_g),
					  Set.of(node) );
	}

	// -------------- helpers --------------

	@SuppressWarnings("deprecation")
	protected Set<TriplePattern> translateAndCollect( final SPARQLGraphPattern pattern ) {
		final SPARQLGraphPattern pattern2;
		if ( pattern instanceof TriplePattern p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof BGP p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof SPARQLGroupPattern p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof SPARQLUnionPattern p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern(p, vm);
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl1 p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern( p.asJenaOp(), vm );
		}
		else if ( pattern instanceof GenericSPARQLGraphPatternImpl2 p ) {
			pattern2 = VocabularyMappingUtils.translateGraphPattern( p.asJenaOp(), vm );
		}
		else {
			throw new IllegalArgumentException( "Unsupported type of pattern: " + pattern.getClass().getName() );
		}

		return pattern2.getAllMentionedTPs();
	}

	protected void assertMappingTranslation( final SPARQLGraphPattern input,
	                                         final Set<TriplePattern> expected ) {
		final Set<TriplePattern> results = translateAndCollect(input);
		assertEquals(expected, results);
	}
}
