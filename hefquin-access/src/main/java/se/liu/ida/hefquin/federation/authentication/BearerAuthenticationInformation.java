package se.liu.ida.hefquin.federation.authentication;

import java.net.http.HttpRequest.Builder;
import java.util.Map;

import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;

/**
 * Authentication information for bearer token authentication.
 * <p>
 * The bearer token is applied to outgoing HTTP requests using an
 * {@code Authorization} header with the {@code Bearer} authentication scheme.
 * This class supports applying the authentication information to different
 * HTTP request representations used by HeFQUIN.
 */
public class BearerAuthenticationInformation implements AuthenticationInformation
{
	private final String token;

	public BearerAuthenticationInformation( final String token ) {
		this.token = token;
	}

	public String getToken() {
		return token;
	}

	/**
	 * Adds the bearer token as an Authorization header to the given HTTP request builder.
	 *
	 * @param builder HTTP request builder to which the Authorization header is added
	 */
	@Override
	public void applyTo( final Builder builder ) {
		builder.setHeader( "Authorization", "Bearer " + token );
	}

	/**
	 * Adds the bearer token as an Authorization header to the given HTTP headers.
	 *
	 * @param headers HTTP headers to which the Authorization header is added
	 */
	@Override
	public void applyTo( final Map<String, String> headers ) {
		headers.put( "Authorization", "Bearer " + token );
	}

	/**
	 * Adds the bearer token as an Authorization header to the given HTTP headers.
	 *
	 * @param b QueryExecutionHTTPBuilder to which the Authorization header is added
	 */
	@Override
	public void applyTo( final QueryExecutionHTTPBuilder b ) {
		b.httpHeader( "Authorization", "Bearer " + token );
	}
}
