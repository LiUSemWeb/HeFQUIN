package se.liu.ida.hefquin.federation.access;

import se.liu.ida.hefquin.federation.FederationMember;

public class FederationAccessException extends Exception
{
	private static final long serialVersionUID = 330224952837746472L;

	/** the request during which this exception occurred */
	protected final DataRetrievalRequest req;

	/** the federation member that was attempted to be accessed */
	protected final FederationMember fm;

	public FederationAccessException( final String message,
	                                  final Throwable cause,
	                                  final DataRetrievalRequest req,
	                                  final FederationMember fm ) {
		super(message, cause);

		this.req = req;
		this.fm = fm;
	}

	public FederationAccessException( final String message,
	                                  final DataRetrievalRequest req,
	                                  final FederationMember fm ) {
		super(message);

		this.req = req;
		this.fm = fm;
	}

	public FederationAccessException( final Throwable cause,
	                                  final DataRetrievalRequest req,
	                                  final FederationMember fm ) {
		super(cause);

		this.req = req;
		this.fm = fm;
	}

	public FederationAccessException( final DataRetrievalRequest req,
	                                  final FederationMember fm ) {
		super();

		this.req = req;
		this.fm = fm;
	}

	/**
	 * Returns the request during which this exception occurred.
	 */
	public DataRetrievalRequest getDataRetrievalRequest() {
		return req;
	}

	/**
	 * Returns the federation member that was attempted to be accessed.
	 */
	public FederationMember getFederationMember() {
		return fm;
	}

}
