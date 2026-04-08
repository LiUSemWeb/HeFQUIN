package se.liu.ida.hefquin.engine.queryplan.executable.impl.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Iterator;

import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.expr.ExprVar;
import org.apache.jena.sparql.expr.NodeValue;
import org.junit.Test;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.base.data.utils.SolutionMappingUtils;
import se.liu.ida.hefquin.engine.queryplan.executable.ExecOpExecutionException;
import se.liu.ida.hefquin.engine.queryplan.executable.impl.CollectingIntermediateResultElementSink;

public class ExecOpUnfoldTest
{
	@Test
	public void unfoldWithError() throws ExecOpExecutionException {
		// Tests the case in which evaluating the
		// UNFOLD expression produces an error.

		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(
				Var.alloc("varIn"),
				NodeFactory.createLiteralString("42") );

		final Expr expr = new ExprVar("unboundVar");
		final Var v1 = Var.alloc("v1");
		final ExecOpUnfold op = new ExecOpUnfold(expr, v1, null, false, null, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		op.process(sm, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );
		assertEquals( sm, it.next() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfoldListFromInputMapping() throws ExecOpExecutionException {
		// Tests the case in which the CDT literal
		// is given via the input solution mapping.

		final String lex = "['hello']";
		final Node lit = NodeFactory.createLiteralDT(lex, CompositeDatatypeList.type);

		final Var varIn = Var.alloc("varIn");
		final SolutionMapping sm = SolutionMappingUtils.createSolutionMapping(varIn, lit);

		final Expr expr = new ExprVar(varIn);
		final Var v1 = Var.alloc("v1");
		final ExecOpUnfold op = new ExecOpUnfold(expr, v1, null, false, null, false);

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		op.process(sm, sink, null);

		final Iterator<SolutionMapping> it = sink.getCollectedSolutionMappings().iterator();

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 2, smOut.size() );
		assertTrue( smOut.contains(varIn) );
		assertTrue( smOut.contains(v1) );
		assertEquals( lit, smOut.get(varIn) );
		assertEquals( NodeFactory.createLiteralString("hello"), smOut.get(v1) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfoldIllFormedList() throws ExecOpExecutionException {
		// Tests the case of a cdt:List literal that is not well formed.

		final String lex = "-not a list-";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runListTest(lex, v1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfoldEmptyList() throws ExecOpExecutionException {
		// Tests the case of a cdt:List literal that represents an empty list.

		final String lex = "[]";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runListTest(lex, v1);

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold1ListWithOneNull() throws ExecOpExecutionException {
		// Tests UNFOLD with one variable for the case of a cdt:List literal
		// that represents a list with a null value as the only element.

		final String lex = "[null]";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runListTest(lex, v1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold1ListWithTwoNulls() throws ExecOpExecutionException {
		// Tests UNFOLD with one variable for the case of a cdt:List literal
		// that represents a list with two null values as its elements.

		final String lex = "[null, null]";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runListTest(lex, v1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold1ListWithNullAndLiteral() throws ExecOpExecutionException {
		// Tests UNFOLD with one variable for the case of a cdt:List literal
		// that represents a list with one null value and one literals as
		// its elements.

		final String lex = "[null, '42']";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runListTest(lex, v1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertTrue( it.hasNext() );
		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 2, smOut.size() );
		assertTrue( smOut.contains(v1) );
		assertEquals( NodeFactory.createLiteralString("42"), smOut.get(v1) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold1ListWithTwoLiterals() throws ExecOpExecutionException {
		// Tests UNFOLD with one variable for the case of a cdt:List literal
		// that represents a list with two literals as its elements.

		final String lex = "['hello', '42']";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runListTest(lex, v1);

		assertTrue( it.hasNext() );

		final Binding smOut1 = it.next().asJenaBinding();
		assertEquals( 2, smOut1.size() );
		assertTrue( smOut1.contains(v1) );
		assertEquals( NodeFactory.createLiteralString("hello"), smOut1.get(v1) );

		assertTrue( it.hasNext() );

		final Binding smOut2 = it.next().asJenaBinding();
		assertEquals( 2, smOut2.size() );
		assertTrue( smOut2.contains(v1) );
		assertEquals( NodeFactory.createLiteralString("42"), smOut2.get(v1) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2ListWithOneNull() throws ExecOpExecutionException {
		// Tests UNFOLD with two variables for the case of a cdt:List literal
		// that represents a list with a null value as the only element.

		final String lex = "[null]";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runListTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 2, smOut.size() );
		assertTrue( smOut.contains(v2) );
		assertEquals( "1", smOut.get(v2).getLiteralLexicalForm() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2ListWithTwoNulls() throws ExecOpExecutionException {
		// Tests UNFOLD with two variables for the case of a cdt:List literal
		// that represents a list with two null values as its elements.

		final String lex = "[null, null]";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runListTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut1 = it.next().asJenaBinding();
		assertEquals( 2, smOut1.size() );
		assertTrue( smOut1.contains(v2) );
		assertEquals( "1", smOut1.get(v2).getLiteralLexicalForm() );

		assertTrue( it.hasNext() );

		final Binding smOut2 = it.next().asJenaBinding();
		assertEquals( 2, smOut2.size() );
		assertTrue( smOut2.contains(v2) );
		assertEquals( "2", smOut2.get(v2).getLiteralLexicalForm() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2ListWithNullAndLiteral() throws ExecOpExecutionException {
		// Tests UNFOLD with two variables for the case of a cdt:List literal
		// that represents a list with one null value and one literals as its
		// elements.

		final String lex = "[null, '42']";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runListTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut1 = it.next().asJenaBinding();
		assertEquals( 2, smOut1.size() );
		assertTrue( smOut1.contains(v2) );
		assertEquals( "1", smOut1.get(v2).getLiteralLexicalForm() );

		assertTrue( it.hasNext() );

		final Binding smOut2 = it.next().asJenaBinding();
		assertEquals( 3, smOut2.size() );
		assertTrue( smOut2.contains(v1) );
		assertTrue( smOut2.contains(v2) );
		assertEquals( "2", smOut2.get(v2).getLiteralLexicalForm() );
		assertEquals( NodeFactory.createLiteralString("42"), smOut2.get(v1) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2ListWithTwoLiterals() throws ExecOpExecutionException {
		// Tests UNFOLD with two variables for the case of a cdt:List literal
		// that represents a list with two literals as its elements.

		final String lex = "['hello', '42']";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runListTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut1 = it.next().asJenaBinding();
		assertEquals( 3, smOut1.size() );
		assertTrue( smOut1.contains(v2) );
		assertEquals( "1", smOut1.get(v2).getLiteralLexicalForm() );
		assertEquals( NodeFactory.createLiteralString("hello"), smOut1.get(v1) );

		assertTrue( it.hasNext() );

		final Binding smOut2 = it.next().asJenaBinding();
		assertEquals( 3, smOut2.size() );
		assertTrue( smOut2.contains(v1) );
		assertTrue( smOut2.contains(v2) );
		assertEquals( "2", smOut2.get(v2).getLiteralLexicalForm() );
		assertEquals( NodeFactory.createLiteralString("42"), smOut2.get(v1) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfoldIllFormedMap() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that is not well formed.

		final String lex = "-not a map-";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfoldEmptyMap() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents an empty map.

		final String lex = "{}";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1);

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfoldMapWithBNodeKey() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a
		// map with a map entry that has a blank node as its key
		// and, thus, is not well formed.

		final String lex = "{_:b : 42}";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1);

		assertTrue( it.hasNext() );
		assertEquals( 1, it.next().asJenaBinding().size() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold1MapWithURIKey() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with a map entry that has a URI as its key.

		final String lex = "{<http://example.org> : 42}";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 2, smOut.size() );
		assertTrue( smOut.contains(v1) );
		assertTrue( smOut.get(v1).isURI() );
		assertEquals( "http://example.org", smOut.get(v1).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold1MapWithLiteralKey() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with a map entry that has a literal as its key.

		final String lex = "{'hello' : 42}";

		final Var v1 = Var.alloc("v1");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 2, smOut.size() );
		assertTrue( smOut.contains(v1) );
		assertTrue( smOut.get(v1).isLiteral() );
		assertEquals( "hello", smOut.get(v1).getLiteralLexicalForm() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2MapWithURIValue() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with a map entry that has a URI as its value.

		final String lex = "{42: <http://example.org>}";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 3, smOut.size() );

		assertTrue( smOut.contains(v1) );
		assertTrue( smOut.get(v1).isLiteral() );
		assertEquals( "42", smOut.get(v1).getLiteralLexicalForm() );

		assertTrue( smOut.contains(v2) );
		assertTrue( smOut.get(v2).isURI() );
		assertEquals( "http://example.org", smOut.get(v2).getURI() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2MapWithLiteralValue() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with a map entry that has a literal as its value.

		final String lex = "{'hello' : 42}";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 3, smOut.size() );

		assertTrue( smOut.contains(v1) );
		assertTrue( smOut.get(v1).isLiteral() );
		assertEquals( "hello", smOut.get(v1).getLiteralLexicalForm() );

		assertTrue( smOut.contains(v2) );
		assertTrue( smOut.get(v2).isLiteral() );
		assertEquals( "42", smOut.get(v2).getLiteralLexicalForm() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2MapWithBNodeValue() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with a map entry that has a blank node as its value.

		final String lex = "{'hello' : _:b}";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 3, smOut.size() );

		assertTrue( smOut.contains(v1) );
		assertTrue( smOut.get(v1).isLiteral() );
		assertEquals( "hello", smOut.get(v1).getLiteralLexicalForm() );

		assertTrue( smOut.contains(v2) );
		assertTrue( smOut.get(v2).isBlank() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2MapWithNullValue() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with a map entry that has null as its value.

		final String lex = "{'hello' : null}";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut = it.next().asJenaBinding();
		assertEquals( 2, smOut.size() );

		assertTrue( smOut.contains(v1) );
		assertTrue( smOut.get(v1).isLiteral() );
		assertEquals( "hello", smOut.get(v1).getLiteralLexicalForm() );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2MapWithSameBNodeValues() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with two map entries that have the same blank node as their
		// respective value.

		final String lex = "{'hello' : _:b, 42: _:b}";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut1 = it.next().asJenaBinding();
		assertEquals( 3, smOut1.size() );
		assertTrue( smOut1.contains(v2) );
		assertTrue( smOut1.get(v2).isBlank() );

		assertTrue( it.hasNext() );

		final Binding smOut2 = it.next().asJenaBinding();
		assertEquals( 3, smOut2.size() );
		assertTrue( smOut2.contains(v2) );
		assertTrue( smOut2.get(v2).isBlank() );

		final Node b1 = smOut1.get(v2);
		final Node b2 = smOut2.get(v2);
		assertTrue( b1.equals(b2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void unfold2MapWithDifferentBNodeValues() throws ExecOpExecutionException {
		// Tests the case of a cdt:Map literal that represents a map
		// with two map entries that have a different blank node as
		// their respective value.

		final String lex = "{'hello' : _:b1, 42: _:b2}";

		final Var v1 = Var.alloc("v1");
		final Var v2 = Var.alloc("v2");
		final Iterator<SolutionMapping> it = runMapTest(lex, v1, v2);

		assertTrue( it.hasNext() );

		final Binding smOut1 = it.next().asJenaBinding();
		assertEquals( 3, smOut1.size() );
		assertTrue( smOut1.contains(v2) );
		assertTrue( smOut1.get(v2).isBlank() );

		assertTrue( it.hasNext() );

		final Binding smOut2 = it.next().asJenaBinding();
		assertEquals( 3, smOut2.size() );
		assertTrue( smOut2.contains(v2) );
		assertTrue( smOut2.get(v2).isBlank() );

		final Node b1 = smOut1.get(v2);
		final Node b2 = smOut2.get(v2);
		assertFalse( b1.equals(b2) );

		assertFalse( it.hasNext() );
	}


	// --------- helpers ---------

	protected Iterator<SolutionMapping> runListTest( final String lex,
	                                                 final Var v1 )
			throws ExecOpExecutionException {
		final Expr expr = NodeValue.makeNode(lex, CompositeDatatypeList.type);
		final ExecOpUnfold op = new ExecOpUnfold(expr, v1, null, false, null, false);

		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(
				Var.alloc("varIn"),
				NodeFactory.createLiteralString("42") );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		op.process(smIn, sink, null);

		// Check that every output solution mapping
		// contains the input solution mapping.
		final Iterable<SolutionMapping> result = sink.getCollectedSolutionMappings();
		final Iterator<SolutionMapping> it = result.iterator();
		while ( it.hasNext() ) {
			final SolutionMapping smOut = it.next();
			assertTrue( SolutionMappingUtils.compatible(smIn, smOut) );
		}

		return result.iterator();
	}

	protected Iterator<SolutionMapping> runListTest( final String lex,
	                                                 final Var v1,
	                                                 final Var v2 )
			throws ExecOpExecutionException {
		final Expr expr = NodeValue.makeNode(lex, CompositeDatatypeList.type);
		final ExecOpUnfold op = new ExecOpUnfold(expr, v1, v2, false, null, false);

		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(
				Var.alloc("varIn"),
				NodeFactory.createLiteralString("42") );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		op.process(smIn, sink, null);

		// Check that every output solution mapping
		// contains the input solution mapping.
		final Iterable<SolutionMapping> result = sink.getCollectedSolutionMappings();
		final Iterator<SolutionMapping> it = result.iterator();
		while ( it.hasNext() ) {
			final SolutionMapping smOut = it.next();
			assertTrue( SolutionMappingUtils.compatible(smIn, smOut) );
		}

		return result.iterator();
	}

	protected Iterator<SolutionMapping> runMapTest( final String lex,
	                                                final Var v1 )
			throws ExecOpExecutionException {
		final Expr expr = NodeValue.makeNode(lex, CompositeDatatypeMap.type);
		final ExecOpUnfold op = new ExecOpUnfold(expr, v1, null, false, null, false);

		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(
				Var.alloc("varIn"),
				NodeFactory.createLiteralString("42") );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		op.process(smIn, sink, null);

		// Check that every output solution mapping
		// contains the input solution mapping.
		final Iterable<SolutionMapping> result = sink.getCollectedSolutionMappings();
		final Iterator<SolutionMapping> it = result.iterator();
		while ( it.hasNext() ) {
			final SolutionMapping smOut = it.next();
			assertTrue( SolutionMappingUtils.compatible(smIn, smOut) );
		}

		return result.iterator();
	}

	protected Iterator<SolutionMapping> runMapTest( final String lex,
	                                                final Var v1,
	                                                final Var v2 )
			throws ExecOpExecutionException {
		final Expr expr = NodeValue.makeNode(lex, CompositeDatatypeMap.type);
		final ExecOpUnfold op = new ExecOpUnfold(expr, v1, v2, false, null, false);

		final SolutionMapping smIn = SolutionMappingUtils.createSolutionMapping(
				Var.alloc("varIn"),
				NodeFactory.createLiteralString("42") );

		final CollectingIntermediateResultElementSink sink = new CollectingIntermediateResultElementSink();
		op.process(smIn, sink, null);

		// Check that every output solution mapping
		// contains the input solution mapping.
		final Iterable<SolutionMapping> result = sink.getCollectedSolutionMappings();
		final Iterator<SolutionMapping> it = result.iterator();
		while ( it.hasNext() ) {
			final SolutionMapping smOut = it.next();
			assertTrue( SolutionMappingUtils.compatible(smIn, smOut) );
		}

		return result.iterator();
	}
}
