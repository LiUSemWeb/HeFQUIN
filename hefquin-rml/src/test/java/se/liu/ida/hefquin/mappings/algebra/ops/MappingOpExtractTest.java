package se.liu.ida.hefquin.mappings.algebra.ops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import se.liu.ida.hefquin.mappings.algebra.BaseForMappingTests;
import se.liu.ida.hefquin.mappings.sources.DataObject;
import se.liu.ida.hefquin.mappings.sources.SourceReference;

public class MappingOpExtractTest extends BaseForMappingTests
{
	@Test
	public void getSchema() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery(),
		                                         "attr2", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		assertEquals( Set.of("attr1", "attr2"), op.getSchema() );
	}

	@Test
	public void isValidInput1() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), new TestDataObject1() );

		assertTrue( op.isValidInput(srMap) );
	}

	@Test
	public void isValidInput2() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();
		srMap.put( op.getSourceReference(), new TestDataObject2() );

		// false because the data object in srMap is not of type TestDataObject1
		assertFalse( op.isValidInput(srMap) );
	}

	@Test
	public void isValidInput3() {
		final Map<String, TestQuery> P = Map.of( "attr1", new TestQuery() );

		final MappingOpExtractForTests op = new MappingOpExtractForTests(P);

		final Map<SourceReference, DataObject> srMap = new HashMap<>();

		// false because srMap doesn't have a data object for
		// the source reference of the operator
		assertFalse( op.isValidInput(srMap) );
	}

}
