package se.liu.ida.hefquin.federation.access.impl.reqproc;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.time.Duration;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpLib;

import se.liu.ida.hefquin.base.net.http.HttpClientProvider;
import se.liu.ida.hefquin.base.utils.BuildInfo;
import se.liu.ida.hefquin.federation.access.FederationAccessException;
import se.liu.ida.hefquin.federation.access.RESTRequest;
import se.liu.ida.hefquin.federation.access.StringResponse;
import se.liu.ida.hefquin.federation.access.impl.response.StringResponseImpl;
import se.liu.ida.hefquin.federation.members.RESTEndpoint;

public class RESTRequestProcessorImpl implements RESTRequestProcessor
{
	protected final HttpClient httpClient;
	protected final long overallTimeout;
	protected final Set<String> endpointsWithLimiters = new HashSet<>();

	/**
	 * Creates the request processor without any thresholds for timeouts.
	 * If you want it with timeouts, use the other constructor.
	 */
	public RESTRequestProcessorImpl() {
		this(-1L, -1L);
	}

	/**
	 * The given timeouts are specified in milliseconds.
	 * Any value {@literal <=} 0 means no timeout.
	 */
	public RESTRequestProcessorImpl( final long connectionTimeout,
	                                 final long overallTimeout ) {
		httpClient = HttpClientProvider.client(connectionTimeout);
		this.overallTimeout = overallTimeout;
	}

	@Override
	public StringResponse performRequest( final RESTRequest req,
	                                      final RESTEndpoint fm ) {
		final URI uri = req.getURI();

		if ( fm.getParallelRequestLimit() != null && endpointsWithLimiters.add(uri.toString()) )
			HttpClientProvider.registerEndpointLimiter(uri.toString(), fm.getParallelRequestLimit());

		final HttpRequest.Builder builder = HttpRequest.newBuilder( uri )
				.header("Accept", "application/json;charset=UTF-8")
				.header("User-Agent", BuildInfo.getUserAgent());

		if ( fm.getAuthenticationInformation() != null )
			fm.getAuthenticationInformation().applyTo( builder );

		if ( overallTimeout > 0L )
			builder.timeout( Duration.ofMillis(overallTimeout) );

		final HttpRequest httpReq = builder.GET().build();

		final Date requestStartTime = new Date();

		final HttpResponse<InputStream> httpResponse;
		try {
			httpResponse = httpClient.send( httpReq, BodyHandlers.ofInputStream() );
		}
		catch ( final IOException e ) {
			final String msg = "Sending a request to the REST API with " +
					"service URI " + fm.getServiceURI() + " resulted in " +
					"an I/O-related exception with the following message: " +
					e.getMessage();
			return new StringResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}
		catch ( final InterruptedException e ) {
			final String msg = "Performing a request at the REST API with " +
					"service URI " + fm.getServiceURI() + " was interrupted " +
					"through an exception with the following message: " +
					e.getMessage();
			return new StringResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		final String body;
		try {
			body = HttpLib.handleResponseRtnString(httpResponse);
		}
		catch ( final HttpException e ) {
			if ( e.getStatusCode() > 0 )
				return new StringResponseImpl( e.getStatusCode(),
				                               e.getMessage(),
				                               requestStartTime );

			final String msg = "Handling the response for a request to " +
					"the REST API with service URI " + fm.getServiceURI() +
					"caused an exception with the following message: " +
					e.getMessage();
			return new StringResponseImpl(
					new FederationAccessException(msg,e,req,fm),
					requestStartTime );
		}

		return new StringResponseImpl(body, requestStartTime);
	}

}
