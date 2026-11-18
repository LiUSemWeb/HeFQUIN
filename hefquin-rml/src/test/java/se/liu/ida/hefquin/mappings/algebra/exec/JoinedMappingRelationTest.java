package se.liu.ida.hefquin.mappings.algebra.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.atlas.lib.Pair;
import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;
import se.liu.ida.hefquin.mappings.algebra.impl.MappingRelationImplWithTupleLayout;

public class JoinedMappingRelationTest
{
	@Test
	public void evaluate_singleJoinCondition() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr2Value") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr4Value") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final int attr1Idx = schemaOut.indexOf("attr1");
		final int attr2Idx = schemaOut.indexOf("attr2");
		final int attr3Idx = schemaOut.indexOf("attr3");
		final int attr4Idx = schemaOut.indexOf("attr4");

		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "commonValue", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue", c.getValueOfCurrentTuple(attr3Idx).getLiteralLexicalForm() );
		assertEquals( "attr4Value", c.getValueOfCurrentTuple(attr4Idx).getLiteralLexicalForm() );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_multipleJoinConditionsMatch() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue1"),
		                    NodeFactory.createLiteralString("commonValue2") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue1"),
		                    NodeFactory.createLiteralString("commonValue2") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3"), Pair.create("attr2", "attr4") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final int attr1Idx = schemaOut.indexOf("attr1");
		final int attr2Idx = schemaOut.indexOf("attr2");
		final int attr3Idx = schemaOut.indexOf("attr3");
		final int attr4Idx = schemaOut.indexOf("attr4");

		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "commonValue1", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue2", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue1", c.getValueOfCurrentTuple(attr3Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue2", c.getValueOfCurrentTuple(attr4Idx).getLiteralLexicalForm() );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_multipleJoinConditionsNoMatches() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue1"),
		                    NodeFactory.createLiteralString("notCommonValue1") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue1"),
		                    NodeFactory.createLiteralString("notCommonValue2") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3"), Pair.create("attr2", "attr4") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final MappingRelationCursor c = r.getCursor();

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_OverlappingSchemas() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr2Value") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);

		final String[] schema2 = { "attr1", "attr4" };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr4Value") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr1") );

		assertThrows( IllegalArgumentException.class, () -> new JoinedMappingRelation(input1, input2, joinConditions) );
	}

	@Test
	public void evaluate_multipleMatches() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr2Value1") };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr2Value2") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1, t2);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t3 = { NodeFactory.createLiteralString("notCommonValue"),
		                    NodeFactory.createLiteralString("attr4Value1") };
		final Node[] t4 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr4Value2") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t3, t4);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final int attr1Idx = schemaOut.indexOf("attr1");
		final int attr2Idx = schemaOut.indexOf("attr2");
		final int attr3Idx = schemaOut.indexOf("attr3");
		final int attr4Idx = schemaOut.indexOf("attr4");

		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "commonValue", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value1", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue", c.getValueOfCurrentTuple(attr3Idx).getLiteralLexicalForm() );
		assertEquals( "attr4Value2", c.getValueOfCurrentTuple(attr4Idx).getLiteralLexicalForm() );

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "commonValue", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value2", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue", c.getValueOfCurrentTuple(attr3Idx).getLiteralLexicalForm() );
		assertEquals( "attr4Value2", c.getValueOfCurrentTuple(attr4Idx).getLiteralLexicalForm() );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_multipleTuples() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue1"),
		                    NodeFactory.createLiteralString("attr2Value1") };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue2"),
		                    NodeFactory.createLiteralString("attr2Value2") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1, t2);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t3 = { NodeFactory.createLiteralString("commonValue1"),
		                    NodeFactory.createLiteralString("attr4Value1") };
		final Node[] t4 = { NodeFactory.createLiteralString("commonValue2"),
		                    NodeFactory.createLiteralString("attr4Value2") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t3, t4);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final int attr1Idx = schemaOut.indexOf("attr1");
		final int attr2Idx = schemaOut.indexOf("attr2");
		final int attr3Idx = schemaOut.indexOf("attr3");
		final int attr4Idx = schemaOut.indexOf("attr4");

		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "commonValue1", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value1", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue1", c.getValueOfCurrentTuple(attr3Idx).getLiteralLexicalForm() );
		assertEquals( "attr4Value1", c.getValueOfCurrentTuple(attr4Idx).getLiteralLexicalForm() );

		assertTrue( c.hasNext() );
		c.advance();

		assertEquals( "commonValue2", c.getValueOfCurrentTuple(attr1Idx).getLiteralLexicalForm() );
		assertEquals( "attr2Value2", c.getValueOfCurrentTuple(attr2Idx).getLiteralLexicalForm() );
		assertEquals( "commonValue2", c.getValueOfCurrentTuple(attr3Idx).getLiteralLexicalForm() );
		assertEquals( "attr4Value2", c.getValueOfCurrentTuple(attr4Idx).getLiteralLexicalForm() );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_noMatches() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr2Value") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t2 = { NodeFactory.createLiteralString("notCommonValue"),
		                    NodeFactory.createLiteralString("attr4Value") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final MappingRelationCursor c = r.getCursor();

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_emptyInput1() {
		final String[] schema1 = { "attr1", "attr2" };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1);

		final String[] schema2 = { "attr3", "attr4" };
		final Node[] t2 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr4Value") };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2, t2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final MappingRelationCursor c = r.getCursor();

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_emptyInput2() {
		final String[] schema1 = { "attr1", "attr2" };
		final Node[] t1 = { NodeFactory.createLiteralString("commonValue"),
		                    NodeFactory.createLiteralString("attr2Value") };
		final MappingRelation input1 = new MappingRelationImplWithTupleLayout(schema1, t1);

		final String[] schema2 = { "attr3", "attr4" };
		final MappingRelation input2 = new MappingRelationImplWithTupleLayout(schema2);

		final List<Pair<String,String>> joinConditions = List.of( Pair.create("attr1", "attr3") );

		final MappingRelation r = new JoinedMappingRelation(input1, input2, joinConditions);

		final List<String> schemaOut = r.getSchema();
		assertEquals( 4, schemaOut.size() );
		assertTrue( schemaOut.contains("attr1") );
		assertTrue( schemaOut.contains("attr2") );
		assertTrue( schemaOut.contains("attr3") );
		assertTrue( schemaOut.contains("attr4") );

		final MappingRelationCursor c = r.getCursor();

		assertFalse( c.hasNext() );
	}
}
