package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingOperator;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithTupleLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;

public class MappingOpUnionTest extends BaseForMappingOperatorTests
{
	@Test
	public void getSchema_Valid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new ConstantMappingOperatorForTests(input1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp2 = new ConstantMappingOperatorForTests(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		assertTrue( op.isValid() );
		assertEquals( Set.of("attr1", "attr2"), op.getSchema() );
	}

	@Test
	public void getSchema_Invalid() {
		final MappingRelation input1 = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp1 = new ConstantMappingOperatorForTests(input1);

		final MappingRelation input2 = new MappingRelationImplWithoutTuples("attr1", "attr3");
		final MappingOperator subOp2 = new ConstantMappingOperatorForTests(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		assertFalse( op.isValid() );
		assertEquals( Set.of("attr1", "attr2", "attr3"), op.getSchema() );
	}

	@Test
	public void evaluate_ColumnInput_FirstAttribute() {
		final String[] schema = { "attr1", "attr2" };

		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema, t1);
		final MappingOperator subOp1 = new ConstantMappingOperatorForTests(input1);

		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema, t2);
		final MappingOperator subOp2 = new ConstantMappingOperatorForTests(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 2, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t1[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t1[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t2[1] );

		assertFalse( c.hasNext() );
	}

}
