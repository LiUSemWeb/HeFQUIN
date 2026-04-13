package se.liu.ida.hefquin.mappings.sources.json;

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

	@Override
	public int hashCode() {
		return jsonPath.hashCode();
	}

	@Override
	public boolean equals( final Object o ) {
		if ( o == this ) return true;

		return     o instanceof JsonPathQuery q
		       &&  q.jsonPath.equals(jsonPath);
	}

	@Override
	public String toString() {
		return jsonPath.getPath();
	}
}
