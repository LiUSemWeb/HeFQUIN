package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;

public class MappingOpUnionTest
{
	@Test
	public void getSchema_Valid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp2 = new MappingOpConstant(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		assertTrue( op.isValid() );
		assertEquals( Set.of("attr1", "attr2"), op.getSchema() );
	}

	@Test
	public void getSchema_Invalid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new MappingOpConstant(input1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr1", "attr3");
		final MappingOperator subOp2 = new MappingOpConstant(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		assertFalse( op.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), op.getSchema() );
	}

}
