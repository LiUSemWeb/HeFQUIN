package se.liu.ida.hefquin.mappings.algebra.sources.json;

import java.util.List;

import org.junit.Test;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONValue;

public class SourceTypeJSONTest
{
	@Test
	public void test() {
		final String json =
				  "{"
				+ "  \"characters\": ["
				+ "      {"
				+ "         \"id\": \"0\","
				+ "         \"firstname\": \"Ash\","
				+ "         \"lastname\": \"Ketchum\","
				+ "         \"hair\": [{\"x\":null}, {\"x\":2}]"
				+ "      },"
				+ "      {"
				+ "         \"id\": \"1\","
				+ "         \"firstname\": \"Misty\","
				+ "         \"hair\": \"orange\""
				+ "      }"
				+ "  ]"
				+ "}";
		final String jsonPathString = "$.characters[*]";

		final JsonObject jsonObject = new JsonObject(json);
		final JsonPathQuery query = new JsonPathQuery(jsonPathString);

		final List<JsonObject> l = SourceTypeJSON.instance.eval(query, jsonObject);

		for ( final JsonObject ctxObj : l ) {
			final ReadContext ctx2 = ctxObj.getReadContext();
			final JsonPath jsonPath2 = JsonPath.compile("hair[*].x");
			final Object r2 = ctx2.read(jsonPath2);
			System.out.println( r2.getClass().getName() );
			System.out.println( r2.toString() );
			if ( r2 instanceof List ll ) {
				System.out.println( ll.get(0).getClass().getName() );
			}
		}
	}

}
