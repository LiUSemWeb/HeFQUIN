package se.liu.ida.hefquin.federation.access.impl.response;

import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;
import se.liu.ida.hefquin.federation.access.RecordsResponse;

import java.util.Date;
import java.util.List;

public class RecordsResponseImpl extends DataRetrievalResponseBase<List<TableRecord>> implements RecordsResponse
{
	/**
	 * Constructs a response with the given records and request start time.
	 * The retrieval end time is automatically set to the current time at the
	 * moment of construction. This constructor assumes no error occurred.
	 *
	 * @param records          the list of records contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public RecordsResponseImpl( final List<TableRecord> records,
	                            final Date requestStartTime ) {
		super(records, requestStartTime);
	}

	/**
	 * Constructs a response with the given records, request start time,
	 * and retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param records          the list of records contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public RecordsResponseImpl( final List<TableRecord> records,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super(records, requestStartTime, retrievalEndTime);
	}

	/**
	 * Constructs a response with the given records, request start time,
	 * and error details. The retrieval end time is automatically set to
	 * the current time at the moment of construction.
	 *
	 * @param records          the list of records contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public RecordsResponseImpl( final List<TableRecord> records,
	                            final Date requestStartTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super(records, requestStartTime, errorStatusCode, errorDescription);
	}

	/**
	 * Constructs a response with the given records, request start time,
	 * retrieval end time, and error details.
	 *
	 * @param records          the list of records contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public RecordsResponseImpl( final List<TableRecord> records,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super(records, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription);
	}
}
