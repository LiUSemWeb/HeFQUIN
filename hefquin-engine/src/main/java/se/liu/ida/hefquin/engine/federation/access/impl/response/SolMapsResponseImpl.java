package se.liu.ida.hefquin.engine.federation.access.impl.response;

import java.util.Date;
import java.util.List;

import se.liu.ida.hefquin.base.data.SolutionMapping;
import se.liu.ida.hefquin.engine.federation.FederationMember;
import se.liu.ida.hefquin.engine.federation.access.DataRetrievalRequest;
import se.liu.ida.hefquin.engine.federation.access.SolMapsResponse;

public class SolMapsResponseImpl
                      extends DataRetrievalResponseBase 
                      implements SolMapsResponse
{
	protected final List<SolutionMapping> solMaps;

	/**
	 * Constructs a response with the given solution mappings, federation member, request, and request start time. The
	 * retrieval end time is automatically set to the current time at the moment of construction. This constructor
	 * assumes no error occurred.
	 *
	 * @param solMaps          the list of solution mappings contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime ) {
		super( fm, request, requestStartTime );

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	/**
	 * Constructs a response with the given solution mappings, federation member, request, request start time, and
	 * retrieval end time. This constructor assumes no error occurred.
	 *
	 * @param solMaps          the list of solution mappings contained in this response (must not be {@code null})
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the request retrieval completed (must not be {@code null})
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime ) {
		super( fm, request, requestStartTime, retrievalEndTime );

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	/**
	 * Constructs a response with the given solution mappings, federation member, request, request start time, and error
	 * details. The retrieval end time is automatically set to the current time at the moment of construction.
	 *
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the request retrieval completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super( fm, request, requestStartTime, errorStatusCode, errorDescription );

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	/**
	 * Constructs a response with the given solution mappings, federation member, request, request start time, retrieval
	 * end time, and error details.
	 *
	 * @param fm               the federation member from which this response originates (must not be {@code null})
	 * @param request          the data retrieval request associated with this response (must not be {@code null})
	 * @param requestStartTime the time at which the request was initiated (must not be {@code null})
	 * @param retrievalEndTime the time at which the request retrieval completed (must not be {@code null})
	 * @param errorStatusCode  the HTTP status code representing an error, or {@code null} if no error occurred
	 * @param errorDescription a short description of the error, or {@code null} if no error occurred
	 */
	public SolMapsResponseImpl( final List<SolutionMapping> solMaps,
	                            final FederationMember fm,
	                            final DataRetrievalRequest request,
	                            final Date requestStartTime,
	                            final Date retrievalEndTime,
	                            final Integer errorStatusCode,
	                            final String errorDescription ) {
		super( fm, request, requestStartTime, retrievalEndTime, errorStatusCode, errorDescription );

		assert solMaps != null;
		this.solMaps = solMaps;
	}

	@Override
	public Iterable<SolutionMapping> getSolutionMappings() {
		return solMaps;
	}

	@Override
	public int getSize() {
		return solMaps.size();
	}
}
