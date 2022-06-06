package se.liu.ida.hefquin.engine.data.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.apache.jena.sparql.core.Var;
import org.apache.jena.sparql.expr.Expr;
import org.apache.jena.sparql.util.ExprUtils;
import org.junit.Test;

import se.liu.ida.hefquin.engine.data.SolutionMapping;

public class FilteringIteratorForSolMaps_ExprListTest
{
	@Test
	public void oneFilterExpression() {
		final Expr lessThan10 = ExprUtils.parse("?x < 10");

		final Node value8  = NodeFactory.createLiteral("8",  XSDDatatype.XSDinteger);
		final Node value9  = NodeFactory.createLiteral("9",  XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Node value15 = NodeFactory.createLiteral("15", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value8) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value15) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value9) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value12) );

		final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ExprList(solmaps, lessThan10);

		assertTrue( it.hasNext() );
		assertEquals( value8, it.next().asJenaBinding().get(x) );

		assertTrue( it.hasNext() );
		assertEquals( value9, it.next().asJenaBinding().get(x) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void twoFilterExpressions() {
		final Expr lessThan13 = ExprUtils.parse("?x < 13");
		final Expr greaterThan8 = ExprUtils.parse("?x > 8");

		final Node value8  = NodeFactory.createLiteral("8",  XSDDatatype.XSDinteger);
		final Node value9  = NodeFactory.createLiteral("9",  XSDDatatype.XSDinteger);
		final Node value12 = NodeFactory.createLiteral("12", XSDDatatype.XSDinteger);
		final Node value15 = NodeFactory.createLiteral("15", XSDDatatype.XSDinteger);
		final Var x = Var.alloc("x");

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value8) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value9) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value12) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(x, value15) );

		final Iterator<SolutionMapping> it = new FilteringIteratorForSolMaps_ExprList(solmaps, lessThan13, greaterThan8);

		assertTrue( it.hasNext() );
		assertEquals( value9, it.next().asJenaBinding().get(x) );

		assertTrue( it.hasNext() );
		assertEquals( value12, it.next().asJenaBinding().get(x) );

		assertFalse( it.hasNext() );
	}

}
