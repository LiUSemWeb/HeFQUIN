package se.liu.ida.hefquin.mappings.algebra.exprs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Map;
import java.util.Set;

import org.apache.jena.atlas.lib.Pair;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.BaseForMappingTests;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpConstant;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpExtend;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpJoin;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpProject;
import se.liu.ida.hefquin.mappings.algebra.ops.MappingOpUnion;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;

public class MappingExpressionFactoryTest extends BaseForMappingTests
{
	@Test
	public void extract_getSchema() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);
		final MappingExpression expr = MappingExpressionFactory.create(op);

		assertEquals( Set.of("attr1", "attr2"), expr.getSchema() );
	}

	@Test
	public void extend_getSchema_Valid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);
		final MappingExpression subExpr = MappingExpressionFactory.create(subOp);

		final ExtendExpression extExpr = new ExtendExprForTests("attr1");

		final MappingOpExtend op = new MappingOpExtend(extExpr, "attr3");
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr);

		assertTrue( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), expr.getSchema() );
	}

	@Test
	public void extend_getSchema_Invalid1() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);
		final MappingExpression subExpr = MappingExpressionFactory.create(subOp);

		// the attribute in the expression is not provided by the input relation
		final ExtendExpression extExpr = new ExtendExprForTests("attrX");

		final MappingOpExtend op = new MappingOpExtend(extExpr, "attr3");
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr);

		assertFalse( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), expr.getSchema() );
	}

	@Test
	public void extend_getSchema_Invalid2() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);
		final MappingExpression subExpr = MappingExpressionFactory.create(subOp);

		final ExtendExpression extExpr = new ExtendExprForTests("attr1");

		// the attribute is already in the input relation
		final MappingOpExtend op = new MappingOpExtend(extExpr, "attr2");
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr);

		assertFalse( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2"), expr.getSchema() );
	}

	@Test
	public void project_getSchema_Valid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);
		final MappingExpression subExpr = MappingExpressionFactory.create(subOp);

		final Set<String> P = Set.of("attr1");

		final MappingOpProject op = new MappingOpProject(P);
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr);

		assertTrue( expr.isValid() );
		assertEquals( P, expr.getSchema() );
	}

	@Test
	public void project_getSchema_Invalid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);
		final MappingExpression subExpr = MappingExpressionFactory.create(subOp);

		final Set<String> P = Set.of("attr1", "attr3");

		final MappingOpProject op = new MappingOpProject(P);
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr);

		assertFalse( expr.isValid() );
		assertEquals( Set.of("attr1"), expr.getSchema() );
	}

	@Test
	public void join_getSchema_Valid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);
		final MappingExpression subExpr1 = MappingExpressionFactory.create(subOp1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr3", "attr4");
		final MappingOperator subOp2 = new MappingOpConstant(input2);
		final MappingExpression subExpr2 = MappingExpressionFactory.create(subOp2);

		final MappingOpJoin op = new MappingOpJoin( new Pair<>("attr1", "attr3") );
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr1, subExpr2);

		assertTrue( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3", "attr4"), expr.getSchema() );
	}

	@Test
	public void join_getSchema_Invalid1() {
		// The issue checked in this test is that the schemas
		// of the two input expressions must be disjoint.

		final MappingRelation input1 = new MappingRelationImplWithoutTuples("same", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);
		final MappingExpression subExpr1 = MappingExpressionFactory.create(subOp1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("same", "attr4");
		final MappingOperator subOp2 = new MappingOpConstant(input2);
		final MappingExpression subExpr2 = MappingExpressionFactory.create(subOp2);

		final MappingOpJoin op = new MappingOpJoin( new Pair<>("attr2", "attr4") );
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr1, subExpr2);

		assertFalse( expr.isValid() );
		assertEquals( Set.of("same", "attr2", "attr4"), expr.getSchema() );
	}

	@Test
	public void join_getSchema_Invalid2() {
		// The issue checked in this test is that the join attributes
		// must exist in the schema of the two input expressions.

		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);
		final MappingExpression subExpr1 = MappingExpressionFactory.create(subOp1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr3", "attr4");
		final MappingOperator subOp2 = new MappingOpConstant(input2);
		final MappingExpression subExpr2 = MappingExpressionFactory.create(subOp2);

		final MappingOpJoin op = new MappingOpJoin( new Pair<>("unknown1", "unknown2") );
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr1, subExpr2);

		assertFalse( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3", "attr4"), expr.getSchema() );
	}

	@Test
	public void union_getSchema_Valid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);
		final MappingExpression subExpr1 = MappingExpressionFactory.create(subOp1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp2 = new MappingOpConstant(input2);
		final MappingExpression subExpr2 = MappingExpressionFactory.create(subOp2);

		final MappingOpUnion op = MappingOpUnion.getInstance();
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr1, subExpr2);

		assertTrue( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2"), expr.getSchema() );
	}

	@Test
	public void union_getSchema_Invalid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);
		final MappingExpression subExpr1 = MappingExpressionFactory.create(subOp1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr1", "attr3");
		final MappingOperator subOp2 = new MappingOpConstant(input2);
		final MappingExpression subExpr2 = MappingExpressionFactory.create(subOp2);

		final MappingOpUnion op = MappingOpUnion.getInstance();
		final MappingExpression expr = MappingExpressionFactory.create(op, subExpr1, subExpr2);

		assertFalse( expr.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), expr.getSchema() );
	}
}
