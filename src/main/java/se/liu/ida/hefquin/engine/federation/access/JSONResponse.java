package se.liu.ida.hefquin.engine.federation.access;

import org.apache.jena.atlas.json.JsonObject;

public interface JSONResponse extends DataRetrievalResponse
{
	/**
	 * @return the json object associated with the JSON response.
	 */
	public JsonObject getJsonObject();
}
