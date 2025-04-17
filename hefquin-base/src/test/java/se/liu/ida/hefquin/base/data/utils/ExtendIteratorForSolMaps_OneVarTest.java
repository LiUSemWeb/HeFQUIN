package se.liu.ida.hefquin.base.data.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
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

import se.liu.ida.hefquin.base.data.SolutionMapping;

public class ExtendIteratorForSolMaps_OneVarTest
{
	@Test
	public void extendTwoSolMaps() {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node lit2 = NodeFactory.createLiteral( "2", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add( SolutionMappingUtils.createSolutionMapping(v1, lit8) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(v1, lit2) );

		final Var v2 = Var.alloc("v2");
		final Expr addOne = ExprUtils.parse("?v1 + 1");
		final Iterator<SolutionMapping> it = new ExtendIteratorForSolMaps_OneVar(solmaps, v2, addOne);

		assertTrue( it.hasNext() );
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		assertEquals( lit9, it.next().asJenaBinding().get(v2) );

		assertTrue( it.hasNext() );
		final Node lit3 = NodeFactory.createLiteral( "3", XSDDatatype.XSDinteger );
		assertEquals( lit3, it.next().asJenaBinding().get(v2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendTwoSolMapsWithOneFailing() {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Node uri = NodeFactory.createURI( "http://example.org" );
		final Var v1 = Var.alloc("v1");

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add( SolutionMappingUtils.createSolutionMapping(v1, lit8) );
		solmaps.add( SolutionMappingUtils.createSolutionMapping(v1, uri) );

		final Var v2 = Var.alloc("v2");
		final Expr addOne = ExprUtils.parse("?v1 + 1"); //should fail for the URI
		final Iterator<SolutionMapping> it = new ExtendIteratorForSolMaps_OneVar(solmaps, v2, addOne);

		assertTrue( it.hasNext() );
		final Node lit9 = NodeFactory.createLiteral( "9", XSDDatatype.XSDinteger );
		assertEquals( lit9, it.next().asJenaBinding().get(v2) );

		assertTrue( it.hasNext() );
		assertFalse( it.next().asJenaBinding().contains(v2) );

		assertFalse( it.hasNext() );
	}

	@Test
	public void extendSolMapWithSameVariable() {
		final Node lit8 = NodeFactory.createLiteral( "8", XSDDatatype.XSDinteger );
		final Var v1 = Var.alloc("v1");

		final List<SolutionMapping> solmaps = new ArrayList<>();
		solmaps.add( SolutionMappingUtils.createSolutionMapping(v1, lit8) );

		final Expr addOne = ExprUtils.parse("?v1 + 1");
		final Iterator<SolutionMapping> it = new ExtendIteratorForSolMaps_OneVar(solmaps, v1, addOne);

		assertTrue( it.hasNext() );
		assertThrows( IllegalArgumentException.class, () -> it.next() );
	}

}
