package se.liu.ida.hefquin.mappings.algebra.sources.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.junit.Test;

public class SourceTypeJSONTest
{
	@Test
	public void eval1_NonMatchingQuery() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.nocharacters[*]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval1_OneObjectSelected() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[0]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0).getReadContext().jsonString().contains("0") );
		assertFalse( result.get(0).getReadContext().jsonString().contains("1") );
	}

	@Test
	public void eval1_OneObjectSelectedFromEmptyArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": [ ]"
				+ "}";
		final String jsonPath = "$.characters[0]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval1_OneObjectSelectedWithNull() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      null,"
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[0]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval1_OneObjectSelectedWithArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      [ { \"id\": \"0\" } ]," // array !
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[0]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0).getReadContext().jsonString().contains("0") );
		assertFalse( result.get(0).getReadContext().jsonString().contains("1") );
	}

	@Test
	public void eval1_OneObjectSelectedWithScalar() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[0].id";  // !!!!

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval1_AllObjectsSelected() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[*]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 2, result.size() );

		final JsonObject resultElmt1 = result.get(0);
		final JsonObject resultElmt2 = result.get(1);

		assertTrue( resultElmt1.getReadContext().jsonString().contains("0") );
		assertTrue( resultElmt2.getReadContext().jsonString().contains("1") );
		assertFalse( resultElmt1.getReadContext().jsonString().contains("1") );
		assertFalse( resultElmt2.getReadContext().jsonString().contains("0") );
	}

	@Test
	public void eval1_AllObjectsSelectedFromEmptyArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": [ ]"
				+ "}";
		final String jsonPath = "$.characters[*]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval1_AllObjectsSelectedWithNull() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      null" // !!!!
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[*]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 1, result.size() );

		final JsonObject resultElmt1 = result.get(0);

		assertTrue( resultElmt1.getReadContext().jsonString().contains("0") );
	}

	@Test
	public void eval1_AllObjectsSelectedWithArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      [{}]" // array !!
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[*]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 1, result.size() );

		final JsonObject resultElmt1 = result.get(0);

		assertTrue( resultElmt1.getReadContext().jsonString().contains("0") );
	}

	@Test
	public void eval1_AllObjectsSelectedWithScalars() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "      { \"id\": 1 },"
				+ "      { \"id\": 1.1 },"
				+ "      { \"id\": true },"
				+ "      { \"id\": null }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[*].id";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_FieldString() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": \"0\" },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "id";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );

		final JsonScalarValue resultElmt1 = result.get(0);

		assertTrue( resultElmt1.getValue() instanceof String );
		assertEquals( "0", resultElmt1.getValue() );
	}

	@Test
	public void eval2_FieldInteger() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": 0 },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "id";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );

		final JsonScalarValue resultElmt1 = result.get(0);

		assertTrue( resultElmt1.getValue() instanceof Integer );
		assertEquals( 0, resultElmt1.getValue() );
	}

	@Test
	public void eval2_FieldNull() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": null },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "id";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_FieldObject() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": {} },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "id";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_FieldArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"id\": [{}] },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "id";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_PathToOneString() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":\"A\", \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );

		final JsonScalarValue resultElmt1 = result.get(0);

		assertTrue( resultElmt1.getValue() instanceof String );
		assertEquals( "A", resultElmt1.getValue() );
	}

	@Test
	public void eval2_PathToOneInteger() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":0, \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );

		final JsonScalarValue resultElmt1 = result.get(0);

		assertTrue( resultElmt1.getValue() instanceof Integer );
		assertEquals( 0, resultElmt1.getValue() );
	}

	@Test
	public void eval2_PathToTwoStrings() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      {"
				+ "        \"name\": ["
				+ "           { \"fn\":\"A\", \"ln\":\"B\" },"
				+ "           { \"fn\":\"C\", \"ln\":\"D\" }"
				+ "        ]"
				+ "      }"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name[*].fn";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 2, result.size() );

		final JsonScalarValue resultElmt1 = result.get(0);
		final JsonScalarValue resultElmt2 = result.get(1);

		assertTrue( resultElmt1.getValue() instanceof String );
		assertTrue( resultElmt2.getValue() instanceof String );
		assertEquals( "A", resultElmt1.getValue() );
		assertEquals( "C", resultElmt2.getValue() );
	}

	@Test
	public void eval2_PathToNull() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":null, \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_PathToObject() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":{}, \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_PathToArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":[{}], \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_PathToArrayContent() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":[1,true], \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn[*]";

		final List<JsonScalarValue> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 2, result.size() );

		final JsonScalarValue resultElmt1 = result.get(0);
		final JsonScalarValue resultElmt2 = result.get(1);

		assertTrue( resultElmt1.getValue() instanceof Integer );
		assertTrue( resultElmt2.getValue() instanceof Boolean );
		assertEquals( 1, resultElmt1.getValue() );
		assertEquals( true, resultElmt2.getValue() );
	}

	@Test
	public void castString() {
		final JsonScalarValue v = new JsonScalarValue("Hello");
		final Node n = SourceTypeJSON.instance.cast(v);

		assertTrue( n.isLiteral() );
		assertEquals( XSDDatatype.XSDstring, n.getLiteralDatatype() );
		assertEquals( "Hello", n.getLiteralLexicalForm() );
	}

	@Test
	public void castInteger() {
		final JsonScalarValue v = new JsonScalarValue(1);
		final Node n = SourceTypeJSON.instance.cast(v);

		assertTrue( n.isLiteral() );
		assertEquals( XSDDatatype.XSDinteger, n.getLiteralDatatype() );
		assertEquals( "1", n.getLiteralLexicalForm() );
	}

	@Test
	public void castDouble() {
		final JsonScalarValue v = new JsonScalarValue(1.1);
		final Node n = SourceTypeJSON.instance.cast(v);

		assertTrue( n.isLiteral() );
		assertEquals( XSDDatatype.XSDdouble, n.getLiteralDatatype() );
		assertEquals( "1.1", n.getLiteralLexicalForm() );
	}

	@Test
	public void castBoolean() {
		final JsonScalarValue v = new JsonScalarValue(true);
		final Node n = SourceTypeJSON.instance.cast(v);

		assertTrue( n.isLiteral() );
		assertEquals( XSDDatatype.XSDboolean, n.getLiteralDatatype() );
		assertEquals( "true", n.getLiteralLexicalForm() );
	}


	// ---------- helpers ---------

	protected List<JsonObject> eval1( final String jsonData,
	                                  final String jsonPath ) {
		final JsonObject jsonObject = new JsonObject(jsonData);
		final JsonPathQuery query = new JsonPathQuery(jsonPath);

		return SourceTypeJSON.instance.eval(query, jsonObject);
	}

	/**
	 * Assumes that the first JSONPath expression selects
	 * exactly one JSON object from the given data.
	 */
	protected List<JsonScalarValue> eval2( final String jsonData,
	                                       final String jsonPath1,
	                                       final String jsonPath2 ) {
		final JsonObject jsonObject = new JsonObject(jsonData);
		final JsonPathQuery query1 = new JsonPathQuery(jsonPath1);
		final JsonPathQuery query2 = new JsonPathQuery(jsonPath2);

		final List<JsonObject> l = SourceTypeJSON.instance.eval(query1, jsonObject);

		final JsonObject ctxObj = l.get(0);
		return SourceTypeJSON.instance.eval(query2, jsonObject, ctxObj);
	}
}
