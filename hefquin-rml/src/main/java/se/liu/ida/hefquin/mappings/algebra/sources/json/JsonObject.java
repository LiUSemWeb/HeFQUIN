package se.liu.ida.hefquin.mappings.algebra.sources.json;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;

import se.liu.ida.hefquin.mappings.algebra.sources.DataObject;

public class JsonObject implements DataObject
{
	protected final ReadContext ctx;

	public JsonObject( final ReadContext ctx ) {
		assert ctx != null;
		this.ctx = ctx;
	}

	public JsonObject( final String jsonString ) throws JsonPathException {
		this( JsonPath.parse(jsonString) );
	}

	public ReadContext getReadContext() {
		return ctx;
	}
}
