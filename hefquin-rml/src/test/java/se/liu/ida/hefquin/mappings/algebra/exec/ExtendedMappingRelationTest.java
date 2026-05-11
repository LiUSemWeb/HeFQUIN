package se.liu.ida.hefquin.mappings.algebra.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.BaseForMappingTests;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithTupleLayout;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;

public class ExtendedMappingRelationTest extends BaseForMappingTests
{
	@Test
	public void evaluate1() {
		// input is a two-column relation with two tuples, and
		// the expression is applied to only one of these columns
		final Node[] t1 = { NodeFactory.createLiteralByValue(1),
		                    NodeFactory.createLiteralByValue(2) };
		final Node[] t2 = { NodeFactory.createLiteralByValue(3),
		                    NodeFactory.createLiteralByValue(4) };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation input = new MappingRelationImplWithTupleLayout(s, t1, t2);

		final ExtendExpression expr = new ExtendExprForTests("attr1");

		final MappingRelation r = new ExtendedMappingRelation(input, expr, "attr3");
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t1[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t1[1] );
		assertEquals( Integer.valueOf(1), c.getValueOfCurrentTuple(2).getLiteralValue() );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t2[1] );
		assertEquals( Integer.valueOf(3), c.getValueOfCurrentTuple(2).getLiteralValue() );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate2() {
		// input is a two-column relation with two tuples, and
		// the expression is applied to both of these columns
		final Node[] t1 = { NodeFactory.createLiteralByValue(1),
		                    NodeFactory.createLiteralByValue(2) };
		final Node[] t2 = { NodeFactory.createLiteralByValue(3),
		                    NodeFactory.createLiteralByValue(4) };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation input = new MappingRelationImplWithTupleLayout(s, t1, t2);

		final ExtendExpression expr = new ExtendExprForTests("attr1", "attr2");

		final MappingRelation r = new ExtendedMappingRelation(input, expr, "attr3");
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t1[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t1[1] );
		assertEquals( Integer.valueOf(1+2), c.getValueOfCurrentTuple(2).getLiteralValue() );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t2[1] );
		assertEquals( Integer.valueOf(3+4), c.getValueOfCurrentTuple(2).getLiteralValue() );

		assertFalse( c.hasNext() );
	}

}
