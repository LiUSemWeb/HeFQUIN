package se.liu.ida.hefquin.mappings.algebra.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;

public class MappingRelationImplWithColumnLayoutTest
{
	@Test
	public void test2col3tuples_CreationBasedOnColumns() {
		final Node bn11 = NodeFactory.createBlankNode();
		final Node bn12 = NodeFactory.createBlankNode();
		final Node bn13 = NodeFactory.createBlankNode();
		final Node bn21 = NodeFactory.createBlankNode();
		final Node bn22 = NodeFactory.createBlankNode();
		final Node bn23 = NodeFactory.createBlankNode();

		final Node[] col1 = { bn11, bn12, bn13 };
		final Node[] col2 = { bn21, bn22, bn23 };

		final MappingRelation r = MappingRelationImplWithColumnLayout.createBasedOnColumns(
				List.of("att1", "att2"),
				col1, col2 );

		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();
		assertEquals( bn11, c.getValueOfCurrentTuple(0) );
		assertEquals( bn21, c.getValueOfCurrentTuple(1) );

		assertTrue( c.hasNext() );
		c.advance();
		assertEquals( bn12, c.getValueOfCurrentTuple(0) );
		assertEquals( bn22, c.getValueOfCurrentTuple(1) );

		assertTrue( c.hasNext() );
		c.advance();
		assertEquals( bn13, c.getValueOfCurrentTuple(0) );
		assertEquals( bn23, c.getValueOfCurrentTuple(1) );

		assertFalse( c.hasNext() );
	}

	@Test
	public void test2col3tuples_CreationBasedOnTuples() {
		final Node bn11 = NodeFactory.createBlankNode();
		final Node bn12 = NodeFactory.createBlankNode();
		final Node bn13 = NodeFactory.createBlankNode();
		final Node bn21 = NodeFactory.createBlankNode();
		final Node bn22 = NodeFactory.createBlankNode();
		final Node bn23 = NodeFactory.createBlankNode();

		final Node[] tuple1 = { bn11, bn21 };
		final Node[] tuple2 = { bn12, bn22 };
		final Node[] tuple3 = { bn13, bn23 };

		final MappingRelation r = MappingRelationImplWithColumnLayout.createBasedOnTuples(
				List.of("att1", "att2"),
		        tuple1, tuple2, tuple3 );

		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();
		assertEquals( bn11, c.getValueOfCurrentTuple(0) );
		assertEquals( bn21, c.getValueOfCurrentTuple(1) );

		assertTrue( c.hasNext() );
		c.advance();
		assertEquals( bn12, c.getValueOfCurrentTuple(0) );
		assertEquals( bn22, c.getValueOfCurrentTuple(1) );

		assertTrue( c.hasNext() );
		c.advance();
		assertEquals( bn13, c.getValueOfCurrentTuple(0) );
		assertEquals( bn23, c.getValueOfCurrentTuple(1) );

		assertFalse( c.hasNext() );
	}
}
