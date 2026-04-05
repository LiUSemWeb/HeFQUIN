package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;

public class MappingOpProjectTest
{
	@Test
	public void getSchema_Valid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);

		final Set<String> P = Set.of("attr1");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		assertTrue( op.isValid() );
		assertEquals( P, op.getSchema() );
	}

	@Test
	public void getSchema_Invalid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new MappingOpConstant(input);

		final Set<String> P = Set.of("attr1", "attr3");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		assertFalse( op.isValid() );
		assertEquals( Set.of("attr1"), op.getSchema() );
	}
}
