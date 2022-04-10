package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;

public class JSONResponseImpl extends DataRetrievalResponseBase implements JSONResponse {

    protected final JsonObject jsonObj;

    public JSONResponseImpl(JsonObject obj, FederationMember fm, DataRetrievalRequest request, Date requestStartTime) {
        super(fm, request, requestStartTime);
        this.jsonObj = obj;
    }

    public JSONResponseImpl(JsonObject obj, FederationMember fm, DataRetrievalRequest request, Date requestStartTime, Date requestEndTime) {
        super(fm, request, requestStartTime,requestEndTime);
        this.jsonObj = obj;
    }

    public JsonObject getJsonObject() {
        return jsonObj;
    }
}
