package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.jena.graph.Node;
import org.apache.jena.graph.NodeFactory;
import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.MappingTuple;
import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;
import se.liu.ida.hefquin.mappings.algebra.sources.SourceReference;

public class MappingOpExtractTest extends BaseForMappingOperatorTests
{
	@Test
	public void getSchema() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );
		P.put( "attr2", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		assertEquals( Set.of("attr1", "attr2"), op.getSchema() );
	}

	@Test
	public void isValidInput1() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), new TestDataObject1() );

		assertTrue( op.isValidInput(srMap) );
	}

	@Test
	public void isValidInput2() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), new TestDataObject2() );

		// false because the data object in srMap is not of type TestDataObject1
		assertFalse( op.isValidInput(srMap) );
	}

	@Test
	public void isValidInput3() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();

		// false because srMap doesn't have a data object for
		// the source reference of the operator
		assertFalse( op.isValidInput(srMap) );
	}

	@Test
	public void evaluate_OneAttr_NoCxtObj() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final TestDataObject1 d1 = new TestDataObject1();

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertFalse( it.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_OneCxtObjWithoutSubObj() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final TestDataObject2 d2 = new TestDataObject2();
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertFalse( it.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_OneCxtObjWithOneSubObj() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Node bnode = NodeFactory.createBlankNode();
		final TestDataObject3 d3 = new TestDataObject3(bnode);
		final TestDataObject2 d2 = new TestDataObject2(d3);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertTrue( it.hasNext() );

		final MappingTuple t = it.next();
		assertEquals( Set.of("attr1"), t.getSchema() );
		assertTrue( t.getValue("attr1") == bnode );

		assertFalse( it.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_OneCxtObjWithTwoSubObjs() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Node bnodeA = NodeFactory.createBlankNode();
		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2 = new TestDataObject2(d3A, d3B);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertTrue( it.hasNext() );

		final Node value1 = it.next().getValue("attr1");
		assertTrue( value1 == bnodeA || value1 == bnodeB );

		assertTrue( it.hasNext() );

		final Node value2 = it.next().getValue("attr1");
		assertTrue( value2 == bnodeA || value2 == bnodeB );
		assertTrue( value2 != value1 );

		assertFalse( it.hasNext() );
	}

	@Test
	public void evaluate_OneAttr_TwoCxtObjsWithOneSubObj() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Node bnodeA = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject2 d2A = new TestDataObject2(d3A);

		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2B = new TestDataObject2(d3B);

		final TestDataObject1 d1 = new TestDataObject1(d2A, d2B);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertTrue( it.hasNext() );

		final Node value1 = it.next().getValue("attr1");
		assertTrue( value1 == bnodeA || value1 == bnodeB );

		assertTrue( it.hasNext() );

		final Node value2 = it.next().getValue("attr1");
		assertTrue( value2 == bnodeA || value2 == bnodeB );
		assertTrue( value2 != value1 );

		assertFalse( it.hasNext() );
	}

	@Test
	public void evaluate_TwoAttrs_OneCxtObjWithOneSubObj() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );
		P.put( "attr2", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Node bnode = NodeFactory.createBlankNode();
		final TestDataObject3 d3 = new TestDataObject3(bnode);
		final TestDataObject2 d2 = new TestDataObject2(d3);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertTrue( it.hasNext() );

		final MappingTuple t = it.next();
		assertEquals( Set.of("attr1", "attr2"), t.getSchema() );
		assertTrue( t.getValue("attr1") == bnode );

		assertFalse( it.hasNext() );
	}

	@Test
	public void evaluate_TwoAttrs_OneCxtObjWithTwoSubObjs() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );
		P.put( "attr2", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Node bnodeA = NodeFactory.createBlankNode();
		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2 = new TestDataObject2(d3A, d3B);
		final TestDataObject1 d1 = new TestDataObject1(d2);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertTrue( it.hasNext() );

		final MappingTuple t1 = it.next();
		assertEquals( Set.of("attr1", "attr2"), t1.getSchema() );
		final Node value11 = t1.getValue("attr1");
		final Node value12 = t1.getValue("attr2");
		assertTrue( value11 == bnodeA || value11 == bnodeB );
		assertTrue( value12 == bnodeA || value12 == bnodeB );
		final int sameValue1 = ( value11 == value12 ) ? 1 : 0;

		assertTrue( it.hasNext() );

		final MappingTuple t2 = it.next();
		assertEquals( Set.of("attr1", "attr2"), t1.getSchema() );
		final Node value21 = t2.getValue("attr1");
		final Node value22 = t2.getValue("attr2");
		assertTrue( value21 == bnodeA || value21 == bnodeB );
		assertTrue( value22 == bnodeA || value22 == bnodeB );
		final int sameValue2 = ( value21 == value22 ) ? 1 : 0;

		assertTrue( it.hasNext() );

		final MappingTuple t3 = it.next();
		assertEquals( Set.of("attr1", "attr2"), t3.getSchema() );
		final Node value31 = t3.getValue("attr1");
		final Node value32 = t3.getValue("attr2");
		assertTrue( value31 == bnodeA || value31 == bnodeB );
		assertTrue( value32 == bnodeA || value32 == bnodeB );
		final int sameValue3 = ( value31 == value32 ) ? 1 : 0;

		assertTrue( it.hasNext() );

		final MappingTuple t4 = it.next();
		assertEquals( Set.of("attr1", "attr2"), t4.getSchema() );
		final Node value41 = t4.getValue("attr1");
		final Node value42 = t4.getValue("attr2");
		assertTrue( value41 == bnodeA || value41 == bnodeB );
		assertTrue( value42 == bnodeA || value42 == bnodeB );
		final int sameValue4 = ( value41 == value42 ) ? 1 : 0;

		assertFalse( it.hasNext() );

		assertEquals( 2, sameValue1 + sameValue2 + sameValue3 + sameValue4 );
	}

	@Test
	public void evaluate_TwoAttrs_TwoCxtObjsWithOneSubObj() {
		final Map<String, TestQuery> P = new HashMap<>();
		P.put( "attr1", new TestQuery() );
		P.put( "attr2", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Node bnodeA = NodeFactory.createBlankNode();
		final TestDataObject3 d3A = new TestDataObject3(bnodeA);
		final TestDataObject2 d2A = new TestDataObject2(d3A);

		final Node bnodeB = NodeFactory.createBlankNode();
		final TestDataObject3 d3B = new TestDataObject3(bnodeB);
		final TestDataObject2 d2B = new TestDataObject2(d3B);

		final TestDataObject1 d1 = new TestDataObject1(d2A, d2B);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), d1 );

		final Iterator<MappingTuple> it = op.evaluate(srMap);
		assertTrue( it.hasNext() );

		final MappingTuple t1 = it.next();
		assertEquals( Set.of("attr1", "attr2"), t1.getSchema() );
		final Node value11 = t1.getValue("attr1");
		final Node value12 = t1.getValue("attr2");
		assertTrue( value11 == bnodeA || value11 == bnodeB );
		assertTrue( value12 == bnodeA || value12 == bnodeB );
		assertTrue( value11 == value12 );

		assertTrue( it.hasNext() );

		final MappingTuple t2 = it.next();
		assertEquals( Set.of("attr1", "attr2"), t1.getSchema() );
		final Node value21 = t2.getValue("attr1");
		final Node value22 = t2.getValue("attr2");
		assertTrue( value21 == bnodeA || value21 == bnodeB );
		assertTrue( value22 == bnodeA || value22 == bnodeB );
		assertTrue( value21 == value22 );

		assertFalse( it.hasNext() );
	}
}
