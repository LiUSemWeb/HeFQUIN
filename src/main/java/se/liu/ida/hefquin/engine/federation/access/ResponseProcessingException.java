package se.liu.ida.hefquin.engine.federation.access;

public class ResponseProcessingException extends FederationAccessException
{
	private static final long serialVersionUID = -1629191632416609651L;

	protected final DataRetrievalResponse response;
	
	public ResponseProcessingException( final String message,
	                                    final Throwable cause,
	                                    final DataRetrievalResponse response ) {
		super( message, cause, response.getRequest(), response.getFederationMember() );

		this.response = response;
	}
	
	public ResponseProcessingException( final String message,
	                                    final DataRetrievalResponse response ) {
		super( message, response.getRequest(), response.getFederationMember() );

		this.response = response;
	}
	
	public ResponseProcessingException( final Throwable cause,
	                                    final DataRetrievalResponse response ) {
		super( cause, response.getRequest(), response.getFederationMember() );

		this.response = response;
	}
	
	public ResponseProcessingException( final DataRetrievalResponse response ) {
		super( response.getRequest(), response.getFederationMember() );

		this.response = response;
	}

	/**
	 * Returns the response that was processed when this exception occurred.
	 */
	public DataRetrievalResponse getDataRetrievalResponse() {
		return response;
	}

}
