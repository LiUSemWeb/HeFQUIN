package se.liu.ida.hefquin.mappings.algebra.sources.json;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;

import se.liu.ida.hefquin.base.query.Query;

public class JsonPathQuery implements Query
{
	final JsonPath jsonPath;

	public JsonPathQuery( final String jsonPathString ) throws JsonPathException {
		this( JsonPath.compile(jsonPathString) );
	}

	public JsonPathQuery( final JsonPath jsonPath ) {
		assert jsonPath != null;
		this.jsonPath = jsonPath;
	}

	public JsonPath getJsonPath() {
		return jsonPath;
	}

	public String getQueryString() {
		return jsonPath.getPath();
	}

}
