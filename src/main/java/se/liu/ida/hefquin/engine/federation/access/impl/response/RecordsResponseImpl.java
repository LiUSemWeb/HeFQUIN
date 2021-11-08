package se.liu.ida.hefquin.engine.federation.access.impl.response;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

import java.util.Date;

public class RecordsResponseImpl extends DataRetrievalResponseBase implements RecordsResponse {

    protected final TableRecord response;

    public RecordsResponseImpl(final TableRecord response, final FederationMember fm,
                                  final DataRetrievalRequest request, final Date requestStartTime) {
        super(fm, request, requestStartTime);

        assert response != null;
        this.response = response;
    }

    public RecordsResponseImpl(final TableRecord response, final FederationMember fm,
                                  final DataRetrievalRequest request, final Date requestStartTime,
                                  final Date retrievalEndTime) {
        super(fm, request, requestStartTime, retrievalEndTime);

        assert response != null;
        this.response = response;
    }

    @Override
    public TableRecord getResponse() {
        return response;
    }
}
