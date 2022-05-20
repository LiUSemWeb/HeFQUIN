package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;

public class JSONResponseImpl extends DataRetrievalResponseBase implements JSONResponse {

    protected final JsonObject jsonObj;

    public JSONResponseImpl(final JsonObject obj, final FederationMember fm, 
            final DataRetrievalRequest request, final Date requestStartTime) {
        super(fm, request, requestStartTime);
        this.jsonObj = obj;
    }

    public JSONResponseImpl(final JsonObject obj, final FederationMember fm, 
            final DataRetrievalRequest request, final Date requestStartTime, 
            final Date requestEndTime) {
        super(fm, request, requestStartTime, requestEndTime);
        this.jsonObj = obj;
    }

    public JsonObject getJsonObject() {
        return jsonObj;
    }
}
