package se.liu.ida.hefquin.federation.authentication;

import java.net.http.HttpRequest.Builder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;

import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;

/**
 * Authentication information for basic authentication.
 * <p>
 * The username and password are combined into a single string by joining them
 * with a colon and then encoded using Base64.
 * <p>
 * The resulting value is applied to outgoing HTTP requests using an
 * {@code Authorization} header with the {@code Basic} authentication scheme.
 * This class supports applying the authentication information to different
 * HTTP request representations used by HeFQUIN.
 */
public class BasicAuthenticationInformation implements AuthenticationInformation
{
	private final String username;
	private final String password;

	public BasicAuthenticationInformation( final String username, final String password ) {
		this.username = username;
		this.password = password;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	/**
	 * Adds the basic token as an Authorization header to the given HTTP request builder.
	 *
	 * @param builder HTTP request builder to which the Authorization header is added
	 */
	@Override
	public void applyTo( final Builder builder ) {
		builder.setHeader( "Authorization", "Basic " + getBasicAuthToken() );
	}

	/**
	 * Adds the basic token as an Authorization header to the given HTTP headers.
	 *
	 * @param headers HTTP headers to which the Authorization header is added
	 */
	@Override
	public void applyTo( final Map<String, String> headers ) {
		headers.put( "Authorization", "Basic " + getBasicAuthToken() );
	}

	/**
	 * Adds the basic token as an Authorization header to the given HTTP headers.
	 *
	 * @param b QueryExecutionHTTPBuilder to which the Authorization header is added
	 */
	@Override
	public void applyTo( final QueryExecutionHTTPBuilder b ) {
		b.httpHeader( "Authorization", "Basic " + getBasicAuthToken() );
	}

	/**
	 * Creates the Base64-encoded credentials used for HTTP Basic Authentication.
	 * The username and password are joined with a colon and the resulting string
	 * is encoded using Base64.
	 *
	 * @return the Base64-encoded username and password
	 */
	private String getBasicAuthToken() {
		final String credentials = username + ":" + password;
		return Base64.getEncoder().encodeToString( credentials.getBytes(StandardCharsets.UTF_8) );
	}
}