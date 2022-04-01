package se.liu.ida.hefquin.engine.federation.access.impl.reqproc;

import se.liu.ida.hefquin.engine.federation.GraphQLEndpoint;
import se.liu.ida.hefquin.engine.federation.access.FederationAccessException;
import se.liu.ida.hefquin.engine.federation.access.GraphQLRequest;
import se.liu.ida.hefquin.engine.federation.access.JSONResponse;

public class GraphQLRequestProcessorImpl implements GraphQLRequestProcessor
{
	protected final int connectionTimeout;
	protected final int readTimeout;

	/**
	 * The given timeouts are specified in milliseconds. Any value {@literal <=} 0 means no timeout.
	 */
	public GraphQLRequestProcessorImpl( final int connectionTimeout, final int readTimeout ) {
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	public GraphQLRequestProcessorImpl() {
		this(-1, -1);
	}

	@Override
	public JSONResponse performRequest( final GraphQLRequest req,
	                                    final GraphQLEndpoint fm )
	                                    		throws FederationAccessException {
		// TODO this function needs to be implemented

		return null;
	}

}
