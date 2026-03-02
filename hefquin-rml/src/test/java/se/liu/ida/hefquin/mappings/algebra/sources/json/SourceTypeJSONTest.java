package se.liu.ida.hefquin.mappings.algebra.sources.json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.jena.cdt.CompositeDatatypeList;
import org.apache.jena.cdt.CompositeDatatypeMap;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.graph.Node;
import org.junit.Test;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;

import net.minidev.json.JSONArray;

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
	public void eval1_OneObjectSelectedButArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      [ { \"id\": \"0\" } ]," // array !
				+ "      { \"id\": \"1\" }"
				+ "  ]"
				+ "}";
		final String jsonPath = "$.characters[0]";

		final List<JsonObject> result = eval1(jsonData, jsonPath);

		assertEquals( 0, result.size() );
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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonScalarValue );

		final JsonScalarValue resultElmt1 = (JsonScalarValue) result.get(0);

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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonScalarValue );

		final JsonScalarValue resultElmt1 = (JsonScalarValue) result.get(0);

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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonObject );
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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonArray );

		final JsonArray arr = (JsonArray) result.get(0);
		assertEquals( 1, arr.getArray().size() );
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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonScalarValue );

		final JsonScalarValue resultElmt1 = (JsonScalarValue) result.get(0);

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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonScalarValue );

		final JsonScalarValue resultElmt1 = (JsonScalarValue) result.get(0);

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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 2, result.size() );
		assertTrue( result.get(0) instanceof JsonScalarValue );
		assertTrue( result.get(1) instanceof JsonScalarValue );

		final JsonScalarValue resultElmt1 = (JsonScalarValue) result.get(0);
		final JsonScalarValue resultElmt2 = (JsonScalarValue) result.get(1);

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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 0, result.size() );
	}

	@Test
	public void eval2_PathToNonEmptyObject() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":{}, \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name";

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonObject );

		final JsonObject obj = (JsonObject) result.get(0);
		final String json = obj.getReadContext().jsonString();
		assertEquals( "{\"fn\":{},\"ln\":\"B\"}", json );
	}

	@Test
	public void eval2_PathToNonEmptyObjectWithNull() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":null, \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name";

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonObject );

		final JsonObject obj = (JsonObject) result.get(0);
		final String json = obj.getReadContext().jsonString();
		assertEquals( "{\"fn\":null,\"ln\":\"B\"}", json );
	}

	@Test
	public void eval2_PathToEmptyObject() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":{}, \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonObject );

		final JsonObject obj = (JsonObject) result.get(0);
		final String json = obj.getReadContext().jsonString();
		assertEquals( "{}", json );
	}

	@Test
	public void eval2_PathToNonEmptyArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":[1,true], \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonArray );

		final JsonArray arr = (JsonArray) result.get(0);
		assertEquals( 2, arr.getArray().size() );
	}

	@Test
	public void eval2_PathToNonEmptyArrayWithNull() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":[1,null], \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonArray );

		final JsonArray arr = (JsonArray) result.get(0);
		assertEquals( 2, arr.getArray().size() );
		assertTrue( arr.getArray().get(1) == null );
	}

	@Test
	public void eval2_PathToEmptyArray() {
		final String jsonData =
				  "{"
				+ "  \"characters\": ["
				+ "      { \"name\": { \"fn\":[], \"ln\":\"B\" } },"
				+ "  ]"
				+ "}";
		final String jsonPath1 = "$.characters[0]";
		final String jsonPath2 = "name.fn";

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 1, result.size() );
		assertTrue( result.get(0) instanceof JsonArray );

		final JsonArray arr = (JsonArray) result.get(0);
		assertEquals( 0, arr.getArray().size() );
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

		final List<JsonElement> result = eval2(jsonData, jsonPath1, jsonPath2);

		assertEquals( 2, result.size() );
		assertTrue( result.get(0) instanceof JsonScalarValue );
		assertTrue( result.get(1) instanceof JsonScalarValue );

		final JsonScalarValue resultElmt1 = (JsonScalarValue) result.get(0);
		final JsonScalarValue resultElmt2 = (JsonScalarValue) result.get(1);

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

	@Test
	public void castNonEmptyArray() {
		final JsonArray arr = new JsonArray( new JSONArray() );
		arr.getArray().add(1);
		arr.getArray().add(true);

		final Node n = SourceTypeJSON.instance.cast(arr);

		assertTrue( n.isLiteral() );
		assertTrue( n.getLiteral().isWellFormed() );

		assertEquals( CompositeDatatypeList.type, n.getLiteralDatatype() );
		assertEquals( "[1,true]", n.getLiteralLexicalForm() );
	}

	@Test
	public void castNonEmptyArrayWithNull() {
		final JsonArray arr = new JsonArray( new JSONArray() );
		arr.getArray().add(1);
		arr.getArray().add(null); // <-- null !

		final Node n = SourceTypeJSON.instance.cast(arr);

		assertTrue( n.isLiteral() );
		assertTrue( n.getLiteral().isWellFormed() );

		assertEquals( CompositeDatatypeList.type, n.getLiteralDatatype() );
		assertEquals( "[1,null]", n.getLiteralLexicalForm() );
	}

	@Test
	public void castEmptyArray() {
		final JsonArray arr = new JsonArray( new JSONArray() );

		final Node n = SourceTypeJSON.instance.cast(arr);

		assertTrue( n.isLiteral() );
		assertTrue( n.getLiteral().isWellFormed() );

		assertEquals( CompositeDatatypeList.type, n.getLiteralDatatype() );
		assertEquals( "[]", n.getLiteralLexicalForm() );
	}

	@Test
	public void castNonEmptyObject() {
		final String str = "{\"key1\":\"value1\",\"key2\":42}";
		final DocumentContext docCtx = JsonPath.parse(str);
		final JsonObject obj = new JsonObject(docCtx);

		final Node n = SourceTypeJSON.instance.cast(obj);

		assertTrue( n.isLiteral() );
		assertTrue( n.getLiteral().isWellFormed() );

		assertEquals( CompositeDatatypeMap.type, n.getLiteralDatatype() );
		assertEquals( str, n.getLiteralLexicalForm() );
	}

	@Test
	public void castNonEmptyObjectWithNull() {
		final String str = "{\"key1\":null,\"key2\":42}";
		final DocumentContext docCtx = JsonPath.parse(str);
		final JsonObject obj = new JsonObject(docCtx);

		final Node n = SourceTypeJSON.instance.cast(obj);

		assertTrue( n.isLiteral() );
		assertTrue( n.getLiteral().isWellFormed() );

		assertEquals( CompositeDatatypeMap.type, n.getLiteralDatatype() );
		assertEquals( str, n.getLiteralLexicalForm() );
	}

	@Test
	public void castEmptyObject() {
		final String str = "{}";
		final DocumentContext docCtx = JsonPath.parse(str);
		final JsonObject obj = new JsonObject(docCtx);

		final Node n = SourceTypeJSON.instance.cast(obj);

		assertTrue( n.isLiteral() );
		assertTrue( n.getLiteral().isWellFormed() );

		assertEquals( CompositeDatatypeMap.type, n.getLiteralDatatype() );
		assertEquals( str, n.getLiteralLexicalForm() );
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
	protected List<JsonElement> eval2( final String jsonData,
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
