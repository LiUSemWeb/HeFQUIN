package se.liu.ida.hefquin.federation.access.impl.response;

import java.util.Date;

import org.apache.jena.atlas.json.JsonObject;

import se.liu.ida.hefquin.federation.access.JSONResponse;

public class JSONResponseImpl extends DataRetrievalResponseBase<JsonObject> implements JSONResponse
{
	/**
	 * Constructs a response with the given JSON object and request start
	 * time. The retrieval end time is automatically set to the current
	 * time at the moment of construction. This constructor assumes no
	 * error occurred.
	 *
	 * @param jsonObject       the JSON object contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public JSONResponseImpl( final JsonObject jsonObject,
	                         final Date requestStartTime ) {
		super(jsonObject, requestStartTime);
	}

	/**
	 * Constructs a response with the given JSON object, request start time,
	 * and retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param jsonObject       the JSON object contained in this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response was completed (must not be {@code null})
	 */
	public JSONResponseImpl( final JsonObject jsonObject,
	                         final Date requestStartTime,
	                         final Date retrievalEndTime ) {
		super(jsonObject, requestStartTime, retrievalEndTime);
	}

	/**
	 * Constructs a response for the case that the response was an error
	 * message. The retrieval end time is automatically set to the
	 * current time at the moment of construction.
	 *
	 * @param errorStatusCode  the HTTP status code of the error message
	 *                         (must not be {@code null})
	 * @param errorDescription a short description of the error (may be
	 *                         {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 */
	public JSONResponseImpl( final int errorStatusCode,
	                         final String errorDescription,
	                         final Date requestStartTime ) {
		super(errorStatusCode, errorDescription, requestStartTime);
	}

	/**
	 * Constructs a response for the case that the response was an error
	 * message.
	 *
	 * @param errorStatusCode  the HTTP status code of the error message
	 *                         (must not be {@code null})
	 * @param errorDescription a short description of the error (may be
	 *                         {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 * @param retrievalEndTime the time at which the retrieval of this response
	 *                         was completed (must not be {@code null})
	 */
	public JSONResponseImpl( final int errorStatusCode,
	                         final String errorDescription,
	                         final Date requestStartTime,
	                         final Date retrievalEndTime ) {
		super(errorStatusCode, errorDescription, requestStartTime, retrievalEndTime);
	}

	/**
	 * Constructs a defective response for the case that an exception
	 * was thrown when producing the response. The retrieval end time
	 * is automatically set to the current time at the moment of
	 * construction.
	 *
	 * @param exception  the exception (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 */
	public JSONResponseImpl( final Exception exception,
	                         final Date requestStartTime ) {
		super(exception, requestStartTime);
	}

	/**
	 * Constructs a defective response for the case that an exception
	 * was thrown when producing the response.
	 *
	 * @param exception  the exception (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated
	 *                         (must not be {@code null})
	 * @param retrievalEndTime the time at which the creation of this response
	 *                         resulted in an exception (must not be {@code null})
	 */
	public JSONResponseImpl( final Exception exception,
	                         final Date requestStartTime,
	                         final Date retrievalEndTime ) {
		super(exception, requestStartTime, retrievalEndTime);
	}
}
