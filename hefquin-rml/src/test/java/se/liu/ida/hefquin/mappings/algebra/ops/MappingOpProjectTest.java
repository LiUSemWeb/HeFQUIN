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
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithColumnLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithTupleLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithoutTuples;

public class MappingOpProjectTest extends BaseForMappingOperatorTests
{
	@Test
	public void getSchema_Valid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new ConstantMappingOperatorForTests(input);

		final Set<String> P = Set.of("attr1");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		assertTrue( op.isValid() );
		assertEquals( P, op.getSchema() );
	}

	@Test
	public void getSchema_Invalid() {
		final MappingRelation input = new MappingRelationImplWithoutTuples("attr1", "attr2");
		final MappingOperator subOp = new ConstantMappingOperatorForTests(input);

		final Set<String> P = Set.of("attr1", "attr3");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		assertFalse( op.isValid() );
		assertEquals( Set.of("attr1"), op.getSchema() );
	}

	@Test
	public void evaluate_ColumnInput_FirstAttribute() {
		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t3 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t4 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t5 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = MappingRelationImplWithColumnLayout.createBasedOnTuples(s, t1, t2, t3, t4, t5);
		final MappingOperator subOp = new ConstantMappingOperatorForTests(r);

		final Set<String> P = Set.of("attr1");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t1[0] );
		assertTrue( c.getValueOfCurrentTuple(0) != t1[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[0] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t3[0] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t4[0] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t5[0] );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_ColumnInput_SecondAttribute() {
		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t3 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t4 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t5 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = MappingRelationImplWithColumnLayout.createBasedOnTuples(s, t1, t2, t3, t4, t5);
		final MappingOperator subOp = new ConstantMappingOperatorForTests(r);

		final Set<String> P = Set.of("attr2");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) != t1[0] );
		assertTrue( c.getValueOfCurrentTuple(0) == t1[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t3[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t4[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t5[1] );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_ColumnInput_BothAttributes() {
		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t3 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t4 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t5 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = MappingRelationImplWithColumnLayout.createBasedOnTuples(s, t1, t2, t3, t4, t5);
		final MappingOperator subOp = new ConstantMappingOperatorForTests(r);

		final Set<String> P = Set.of("attr1", "attr2");

		final MappingOpProject op = new MappingOpProject(subOp, P);

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

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t3[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t3[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t4[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t4[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t5[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t5[1] );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_TupleInput_FirstAttribute() {
		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t3 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t4 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t5 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = new MappingRelationImplWithTupleLayout(s, t1, t2, t3, t4, t5);
		final MappingOperator subOp = new ConstantMappingOperatorForTests(r);

		final Set<String> P = Set.of("attr1");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t1[0] );
		assertTrue( c.getValueOfCurrentTuple(0) != t1[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[0] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t3[0] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t4[0] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t5[0] );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_TupleInput_SecondAttribute() {
		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t3 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t4 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t5 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = new MappingRelationImplWithTupleLayout(s, t1, t2, t3, t4, t5);
		final MappingOperator subOp = new ConstantMappingOperatorForTests(r);

		final Set<String> P = Set.of("attr2");

		final MappingOpProject op = new MappingOpProject(subOp, P);

		final MappingRelation rOut = op.evaluate(null);
		final List<String> schemaOut = rOut.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = rOut.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) != t1[0] );
		assertTrue( c.getValueOfCurrentTuple(0) == t1[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t2[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t3[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t4[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t5[1] );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_TupleInput_BothAttributes() {
		final Node[] t1 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t2 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t3 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t4 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };
		final Node[] t5 = { NodeFactory.createBlankNode(),
		                    NodeFactory.createBlankNode() };

		final List<String> s = List.of("attr1", "attr2");
		final MappingRelation r = new MappingRelationImplWithTupleLayout(s, t1, t2, t3, t4, t5);
		final MappingOperator subOp = new ConstantMappingOperatorForTests(r);

		final Set<String> P = Set.of("attr1", "attr2");

		final MappingOpProject op = new MappingOpProject(subOp, P);

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

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t3[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t3[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t4[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t4[1] );

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == t5[0] );
		assertTrue( c.getValueOfCurrentTuple(1) == t5[1] );

		assertFalse( c.hasNext() );
	}
}
