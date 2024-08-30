package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.wrappers.lpgwrapper.data.TableRecord;

import java.util.List;

public interface RecordsResponse extends DataRetrievalResponse {
    List<TableRecord> getResponse();
}
