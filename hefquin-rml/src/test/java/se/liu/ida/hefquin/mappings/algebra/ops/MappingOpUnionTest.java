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

	@Test
	public void evaluate_SameInputSchema() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("attr1Value1"),
		                    NodeFactory.createLiteralString("attr2Value1") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);
		final MappingOperator subOp1 = new MappingOpConstant(input1);

		final String[] schema2 = { "attr1", "attr2" };
		final Node[] t2 = { NodeFactory.createLiteralString("attr1Value2"),
		                    NodeFactory.createLiteralString("attr2Value2") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);
		final MappingOperator subOp2 = new MappingOpConstant(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 2, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );

		final int attr1Idx = schemaOut.indexOf("attr1");
		final int attr2Idx = schemaOut.indexOf("attr2");

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "attr1Value1", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value1", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "attr1Value2", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value2", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_DifferentInputSchemas() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("attr1Value1"),
		                    NodeFactory.createLiteralString("attr2Value1") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);
		final MappingOperator subOp1 = new MappingOpConstant(input1);

		final String[] schema2 = { "attr2", "attr1" };
		final Node[] t2 = { NodeFactory.createLiteralString("attr2Value2"),
		                    NodeFactory.createLiteralString("attr1Value2") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);
		final MappingOperator subOp2 = new MappingOpConstant(input2);

		final MappingOpUnion op = new MappingOpUnion(subOp1, subOp2);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 2, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );

		final int attr1Idx = schemaOut.indexOf("attr1");
		final int attr2Idx = schemaOut.indexOf("attr2");

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "attr1Value1", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value1", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "attr1Value2", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value2", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );

		assertFalse( c.hasNext() );
	}

}
