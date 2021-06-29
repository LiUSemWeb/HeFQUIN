package se.liu.ida.hefquin.engine.federation.access.impl.response;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.StringRetrievalResponse;

import java.util.Date;

public class StringResponseImpl extends DataRetrievalResponseBase implements StringRetrievalResponse {

    protected final String response;

    public StringResponseImpl( final String response,
                                  final FederationMember fm,
                                  final DataRetrievalRequest request,
                                  final Date requestStartTime) {
        super(fm, request, requestStartTime);

        assert response != null;
        this.response = response;
    }

    public StringResponseImpl( final String response,
                                  final FederationMember fm,
                                  final DataRetrievalRequest request,
                                  final Date requestStartTime,
                                  final Date retrievalEndTime) {
        super(fm, request, requestStartTime, retrievalEndTime);

        assert response != null;
        this.response = response;
    }

    @Override
    public String getResponse() {
        return this.response;
    }
}
