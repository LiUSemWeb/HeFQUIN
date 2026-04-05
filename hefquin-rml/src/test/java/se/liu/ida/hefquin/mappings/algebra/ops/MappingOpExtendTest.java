package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.BaseForMappingTests;
import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;
import se.liu.ida.hefquin.mappings.algebra.ops.extexprs.ExtendExpression;

public class MappingOpExtendTest extends BaseForMappingTests
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

}
