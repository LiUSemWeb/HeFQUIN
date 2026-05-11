package se.liu.ida.hefquin.mappings.sources.json;

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.JsonPathException;
import com.jayway.jsonpath.ReadContext;

public class JsonObject extends JsonElement
{
	protected final ReadContext ctx;

	public JsonObject( final ReadContext ctx ) {
		assert ctx != null;
		this.ctx = ctx;
	}

	public JsonObject( final String jsonString ) throws JsonPathException {
		this( JsonPath.parse(jsonString, SourceTypeJSON.conf) );
	}

	public ReadContext getReadContext() {
		return ctx;
	}
}
