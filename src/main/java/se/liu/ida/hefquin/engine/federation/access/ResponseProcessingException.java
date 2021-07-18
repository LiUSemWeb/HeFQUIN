package se.liu.ida.hefquin.engine.federation.access;

import se.liu.ida.hefquin.engine.federation.FederationMember;

public class ResponseProcessingException extends FederationAccessException
{
	private static final long serialVersionUID = -1629191632416609651L;

	protected final DataRetrievalResponse response;
	
	public ResponseProcessingException( final String message,
	                                    final Throwable cause,
	                                    final DataRetrievalResponse response,
	                                    final DataRetrievalRequest request,
	                                    final FederationMember fm ) {
		super(message, cause, request, fm);

		this.response = response;
	}
	
	public ResponseProcessingException( final String message,
	                                    final DataRetrievalResponse response,
	                                    final DataRetrievalRequest request,
	                                    final FederationMember fm ) {
		super(message, request, fm);

		this.response = response;
	}
	
	public ResponseProcessingException( final Throwable cause,
	                                    final DataRetrievalResponse response,
	                                    final DataRetrievalRequest request,
	                                    final FederationMember fm ) {
		super(cause, request, fm);

		this.response = response;
	}
	
	public ResponseProcessingException( final DataRetrievalResponse response,
	                                    final DataRetrievalRequest request,
	                                    final FederationMember fm ) {
		super(request, fm);

		this.response = response;
	}

	/**
	 * Returns the response that was processed when this exception occurred.
	 */
	public DataRetrievalResponse getDataRetrievalResponse() {
		return response;
	}

}
