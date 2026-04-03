package se.liu.ida.hefquin.mappings.sources.json;

import net.minidev.json.JSONArray;

public class JsonArray extends JsonElement
{
	protected final JSONArray arr;

	public JsonArray( final JSONArray arr ) {
		assert arr != null;
		this.arr = arr;
	}

	public JSONArray getArray() { return arr; }

	@Override
	public String toString() { return arr.toString(); }

	@Override
	public int hashCode() { return arr.hashCode(); }

	@Override
	public boolean equals( final Object o ) { return arr.equals(o); }
}
