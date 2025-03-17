package se.liu.ida.hefquin.engine.federation.access.impl.response;

import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.RecordsResponse;
import se.liu.ida.hefquin.engine.wrappers.lpg.data.TableRecord;

import java.util.Date;
import java.util.List;

public class RecordsResponseImpl extends DataRetrievalResponseBase implements RecordsResponse
{
	protected final List<TableRecord> response;

	/**
	 * Constructs a response with the given records, federation member, request, and request start time. The retrieval
	 * end time is automatically set to the current time at the moment of construction. This constructor assumes no
	 * error occurred.
	 *
	 * @param response         the list of records contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public RecordsResponseImpl( final List<TableRecord> response,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime ) {
		super( fm, request, requestStartTime );

		assert response != null;
		this.response = response;
	}

	/**
	 * Constructs a response with the given records, federation member, request, request start time, and retrieval end
	 * time. This constructor assumes no error occurred.
	 *
	 * @param response         the list of records contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public RecordsResponseImpl( final List<TableRecord> response,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super( fm, request, requestStartTime, retrievalEndTime );

		assert response != null;
		this.response = response;
	}

	/**
	 * Constructs a response with the given records, federation member, request, request start time, and error details..
	 * The retrieval end time is automatically set to the current time at the moment of construction.
	 *
	 * @param response         the list of records contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public RecordsResponseImpl( final List<TableRecord> response,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super( fm, request, requestStartTime, errorStatusCode, errorDescription );

		assert response != null;
		this.response = response;
	}

	/**
	 * Constructs a response with the given records, federation member, request, request start time, retrieval end time,
	 * and error details.
	 *
	 * @param response         the list of records contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public RecordsResponseImpl( final List<TableRecord> response,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super( fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );

		assert response != null;
		this.response = response;
	}

	@Override
	public List<TableRecord> getResponse() {
		return response;
	}
}
