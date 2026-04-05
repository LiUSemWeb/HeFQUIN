package se.liu.ida.hefquin.mappings.algebra.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithColumnLayout;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithTupleLayout;

public class ProjectedMappingRelationTest
{
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
		final MappingRelation input = MappingRelationImplWithColumnLayout.createBasedOnTuples(s, t1, t2, t3, t4, t5);

		final List<String> P = List.of("attr1");

		final MappingRelation r = new ProjectedMappingRelation(P, input);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );

		final MappingRelationCursor c = r.getCursor();

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
		final MappingRelation input = MappingRelationImplWithColumnLayout.createBasedOnTuples(s, t1, t2, t3, t4, t5);

		final List<String> P = List.of("attr2");

		final MappingRelation r = new ProjectedMappingRelation(P, input);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = r.getCursor();

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
		final MappingRelation input = MappingRelationImplWithColumnLayout.createBasedOnTuples(s, t1, t2, t3, t4, t5);

		final List<String> P = List.of("attr1", "attr2");

		final MappingRelation r = new ProjectedMappingRelation(P, input);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 2, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = r.getCursor();

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
		final MappingRelation input = new MappingRelationImplWithTupleLayout(s, t1, t2, t3, t4, t5);

		final List<String> P = List.of("attr1");

		final MappingRelation r = new ProjectedMappingRelation(P, input);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );

		final MappingRelationCursor c = r.getCursor();

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
		final MappingRelation input = new MappingRelationImplWithTupleLayout(s, t1, t2, t3, t4, t5);

		final List<String> P = List.of("attr2");

		final MappingRelation r = new ProjectedMappingRelation(P, input);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 1, schemaOut.size() );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = r.getCursor();

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
		final MappingRelation input = new MappingRelationImplWithTupleLayout(s, t1, t2, t3, t4, t5);

		final List<String> P = List.of("attr1", "attr2");

		final MappingRelation r = new ProjectedMappingRelation(P, input);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 2, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );

		final MappingRelationCursor c = r.getCursor();

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
