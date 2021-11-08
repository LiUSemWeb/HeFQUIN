package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

public interface RecordsResponse extends DataRetrievalResponse {
    TableRecord getResponse();
}
