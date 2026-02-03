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

import org.apache.jena.atlas.web.HttpException;
import org.apache.jena.http.HttpLib;

import se.liu.ida.hefquin.base.shared.http.HttpClientProvider;
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
		httpClient = createHttpClient(connectionTimeout);
		this.overallTimeout = overallTimeout;
	}

	protected static HttpClient createHttpClient( final long connectionTimeout ) {
		return HttpClientProvider.client(connectionTimeout);
	}

	@Override
	public StringResponse performRequest( final RESTRequest req,
	                                      final RESTEndpoint fm )
			throws FederationAccessException
	{
		final URI uri = req.getURI();
		final HttpRequest.Builder builder = HttpRequest.newBuilder( uri)
				.header("Accept", "application/json;charset=UTF-8")
				.header("User-Agent", BuildInfo.getUserAgent());

		if ( overallTimeout > 0L )
			builder.timeout( Duration.ofMillis(overallTimeout) );

		final HttpRequest httpReq = builder.GET().build();

		final Date requestStartTime = new Date();

		final HttpResponse<InputStream> httpResponse;
		try {
			httpResponse = httpClient.send( httpReq, BodyHandlers.ofInputStream() );
		}
		catch ( final IOException e ) {
			throw new FederationAccessException( "Request to REST API at <" + uri.toString() + "> failed: " + e.getMessage(), e, req, fm );
		}
		catch ( final InterruptedException e ) {
			throw new FederationAccessException( "Request to REST API at <" + uri.toString() + "> failed: " + e.getMessage(), e, req, fm );
		}

		final String body;
		try {
			body = HttpLib.handleResponseRtnString(httpResponse);
		}
		catch ( final HttpException e ) {
			throw new FederationAccessException( "Unexpected response for request to REST API (requested URI: <" + uri.toString() + ">, message: " + e.getMessage() + ")", e, req, fm );
		}

		return new StringResponseImpl(body, fm, req, requestStartTime);
	}

}
