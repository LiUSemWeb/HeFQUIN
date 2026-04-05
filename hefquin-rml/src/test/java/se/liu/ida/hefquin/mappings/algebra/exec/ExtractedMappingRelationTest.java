package se.liu.ida.hefquin.mappings.algebra.exec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.BaseForMappingTests;
import se.liu.ida.hefquin.mappings.algebra.MappingRelation;
import se.liu.ida.hefquin.mappings.algebra.MappingRelationCursor;

public class ExtractedMappingRelationTest extends BaseForMappingTests
{
	@Test
	public void evaluate_OneAttr_NoCxtObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final TestDataObject1 d1 = new TestDataObject1();

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_OneCxtObjWithoutSubObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final TestDataObject2 d2 = new TestDataObject2();
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_OneCxtObjWithOneSubObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final Node bnode = NodeFactory.createBlankNode();
		final TestDataObject3 d3 = new TestDataObject3(bnode);
		final TestDataObject2 d2 = new TestDataObject2(d3);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == bnode );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_OneCxtObjWithTwoSubObjs() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final Node bnodeA = NodeFactory.createBlankNode();
		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2 = new TestDataObject2(d3A, d3B);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		final Node value1 = c.getValueOfCurrentTuple(0);
		assertTrue( value1 == bnodeA || value1 == bnodeB );

		assertTrue( c.hasNext() );
		c.advance();

		final Node value2 = c.getValueOfCurrentTuple(0);
		assertTrue( value2 == bnodeA || value2 == bnodeB );
		assertTrue( value2 != value1 );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_TwoCxtObjsWithOneSubObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final Node bnodeA = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject2 d2A = new TestDataObject2(d3A);

		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2B = new TestDataObject2(d3B);

		final TestDataObject1 d1 = new TestDataObject1(d2A, d2B);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		final Node value1 = c.getValueOfCurrentTuple(0);
		assertTrue( value1 == bnodeA || value1 == bnodeB );

		assertTrue( c.hasNext() );
		c.advance();

		final Node value2 = c.getValueOfCurrentTuple(0);
		assertTrue( value2 == bnodeA || value2 == bnodeB );
		assertTrue( value2 != value1 );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_TwoAttrs_OneCxtObjWithOneSubObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );

		final Node bnode = NodeFactory.createBlankNode();
		final TestDataObject3 d3 = new TestDataObject3(bnode);
		final TestDataObject2 d2 = new TestDataObject2(d3);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == bnode );
		assertTrue( c.getValueOfCurrentTuple(1) == bnode );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_TwoAttrs_OneCxtObjWithTwoSubObjs() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );

		final Node bnodeA = NodeFactory.createBlankNode();
		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2 = new TestDataObject2(d3A, d3B);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		final Node value11 = c.getValueOfCurrentTuple(0);
		final Node value12 = c.getValueOfCurrentTuple(1);
		assertTrue( value11 == bnodeA || value11 == bnodeB );
		assertTrue( value12 == bnodeA || value12 == bnodeB );
		final int sameValue1 = ( value11 == value12 ) ? 1 : 0;

		assertTrue( c.hasNext() );
		c.advance();

		final Node value21 = c.getValueOfCurrentTuple(0);
		final Node value22 = c.getValueOfCurrentTuple(1);
		assertTrue( value21 == bnodeA || value21 == bnodeB );
		assertTrue( value22 == bnodeA || value22 == bnodeB );
		final int sameValue2 = ( value21 == value22 ) ? 1 : 0;

		assertTrue( c.hasNext() );
		c.advance();

		final Node value31 = c.getValueOfCurrentTuple(0);
		final Node value32 = c.getValueOfCurrentTuple(1);
		assertTrue( value31 == bnodeA || value31 == bnodeB );
		assertTrue( value32 == bnodeA || value32 == bnodeB );
		final int sameValue3 = ( value31 == value32 ) ? 1 : 0;

		assertTrue( c.hasNext() );
		c.advance();

		final Node value41 = c.getValueOfCurrentTuple(0);
		final Node value42 = c.getValueOfCurrentTuple(1);
		assertTrue( value41 == bnodeA || value41 == bnodeB );
		assertTrue( value42 == bnodeA || value42 == bnodeB );
		final int sameValue4 = ( value41 == value42 ) ? 1 : 0;

		assertFalse( c.hasNext() );

		assertEquals( 2, sameValue1 + sameValue2 + sameValue3 + sameValue4 );
	}

	@Test
	public void evaluate_TwoAttrs_TwoCxtObjsWithOneSubObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );

		final Node bnodeA = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject2 d2A = new TestDataObject2(d3A);

		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2B = new TestDataObject2(d3B);

		final TestDataObject1 d1 = new TestDataObject1(d2A, d2B);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);
		final MappingRelationCursor c = r.getCursor();

		assertTrue( c.hasNext() );
		c.advance();

		final Node value11 = c.getValueOfCurrentTuple(0);
		final Node value12 = c.getValueOfCurrentTuple(1);
		assertTrue( value11 == bnodeA || value11 == bnodeB );
		assertTrue( value12 == bnodeA || value12 == bnodeB );
		assertTrue( value11 == value12 );

		assertTrue( c.hasNext() );
		c.advance();

		final Node value21 = c.getValueOfCurrentTuple(0);
		final Node value22 = c.getValueOfCurrentTuple(1);
		assertTrue( value21 == bnodeA || value21 == bnodeB );
		assertTrue( value22 == bnodeA || value22 == bnodeB );
		assertTrue( value21 == value22 );

		assertFalse( c.hasNext() );
	}

	@Test
	public void evaluate_FourAttrs_OneCxtObjWithOneSubObj() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery(),
		                                         "attr3", new TestQuery(),
		                                         "attr4", new TestQuery() );

		final Node bnode = NodeFactory.createBlankNode();
		final TestDataObject3 d3 = new TestDataObject3(bnode);
		final TestDataObject2 d2 = new TestDataObject2(d3);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final MappingRelation r = new ExtractedMappingRelationForTests(P, d1);

		final List<String> schema = r.getSchema();
		assertEquals( 4, schema.size() );
		assertTrue( schema.contains("attr1") );
		assertTrue( schema.contains("attr2") );
		assertTrue( schema.contains("attr3") );
		assertTrue( schema.contains("attr4") );

		final MappingRelationCursor c = r.getCursor();
		assertTrue( c.hasNext() );
		c.advance();

		assertTrue( c.getValueOfCurrentTuple(0) == bnode );
		assertTrue( c.getValueOfCurrentTuple(1) == bnode );
		assertTrue( c.getValueOfCurrentTuple(2) == bnode );
		assertTrue( c.getValueOfCurrentTuple(3) == bnode );

		assertFalse( c.hasNext() );
	}


	static class ExtractedMappingRelationForTests
			extends ExtractedMappingRelation<TestDataObject1,
			                                 TestDataObject2,
			                                 TestDataObject3,
			                                 TestQuery,
			                                 TestQuery> {
		public ExtractedMappingRelationForTests( final Map<String, TestQuery> P,
		                                         final TestDataObject1 d ) {
			super( new ArrayList<>(P.keySet()),
			       new SourceTypeForTests(),
			       new TestQuery(),
			       P.entrySet(),
			       d );
		}
	}

}
