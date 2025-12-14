package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.exprs.ExtendExpression;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithTupleLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;

public class MappingOpExtendTest extends BaseForMappingOperatorTests
{
	@Test
	public void getSchema_Valid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);

		final ExtendExpression expr = new ExtendExprForTests("attr1");

		final MappingOpExtend op = new MappingOpExtend(subOp, expr, "attr3");

		assertTrue( op.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), op.getSchema() );
	}

	@Test
	public void getSchema_Invalid1() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);

		// the attribute in the expression is not provided by the input relation
		final ExtendExpression expr = new ExtendExprForTests("attrX");

		final MappingOpExtend op = new MappingOpExtend(subOp, expr, "attr3");

		assertFalse( op.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), op.getSchema() );
	}

	@Test
	public void getSchema_Invalid2() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);

		final ExtendExpression expr = new ExtendExprForTests("attr1");

		// the attribute is already in the input relation
		final MappingOpExtend op = new MappingOpExtend(subOp, expr, "attr2");

		assertFalse( op.isValid() );
		assertEquals( Set.of("attr1", "attr2"), op.getSchema() );
	}

	@Test
	public void evaluate1() {
		// input is a two-column relation with two tuples, and
		// the expression is applied to only one of these columns
		final Node[] t1 = { NodeFactory.createLiteralByValue(1),
		                    NodeFactory.createLiteralByValue(2) };
		final Node[] t2 = { NodeFactory.createLiteralByValue(3),
		                    NodeFactory.createLiteralByValue(4) };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = new MappingRelationImplWithTupleLayout(s, t1, t2);
		final MappingOperator subOp = new MappingOpConstant(r);

		final ExtendExpression expr = new ExtendExprForTests("attr1");

		final MappingOpExtend op = new MappingOpExtend(subOp, expr, "attr3");

		final MappingRelationCursor c = op.evaluate(null).getCursor();

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
		final MappingRelation r = new MappingRelationImplWithTupleLayout(s, t1, t2);
		final MappingOperator subOp = new MappingOpConstant(r);

		final ExtendExpression expr = new ExtendExprForTests("attr1", "attr2");

		final MappingOpExtend op = new MappingOpExtend(subOp, expr, "attr3");

		final MappingRelationCursor c = op.evaluate(null).getCursor();

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


	// ----------- helper ----------

	public static class ExtendExprForTests implements ExtendExpression {
		final Set<String> A;

		public ExtendExprForTests( final String ... A ) { this( Arrays.asList(A) ); }

		public ExtendExprForTests( final List<String> A ) { this( new HashSet<>(A) ); }

		public ExtendExprForTests( final Set<String> A ) { this.A = A; }

		@Override
		public Set<String> getAllMentionedAttributes() { return A; }

		@Override
		public Node evaluate( final Map<String, Node> assignment ) {
			int sum = 0;
			for ( final Map.Entry<String, Node> e : assignment.entrySet() ) {
				final Object v = e.getValue().getLiteralValue();
				if ( v instanceof Integer i ) {
					sum += i;
				}
			}

			final Node n = NodeFactory.createLiteralByValue(sum);
			return n;
		}
	}

}
