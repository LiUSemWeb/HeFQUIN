package se.liu.ida.hefquin.federation.authentication;

import java.net.http.HttpRequest.Builder;
import java.util.Map;

import org.apache.jena.sparql.exec.http.QueryExecutionHTTPBuilder;

/**
 * Authentication information for token-based authentication.
 * <p>
 * The token is applied to outgoing HTTP requests using an
 * {@code Authorization} header with the configured authentication scheme.
 * If no authentication scheme is specified, {@code Bearer} is used by default.
 * This class supports applying the authentication information to different
 * HTTP request representations used by HeFQUIN.
 */
public class TokenBasedAuthenticationInformation implements AuthenticationInformation
{
	private final String authenticationScheme;
	private final String token;

	public TokenBasedAuthenticationInformation( final String token ) {
		this( "Bearer", token );
	}

	public TokenBasedAuthenticationInformation( final String authenticationScheme, final String token ) {
		this.authenticationScheme = authenticationScheme;
		this.token = token;
	}

	public String getAuthenticationScheme() {
		return authenticationScheme;
	}

	public String getToken() {
		return token;
	}

	/**
	 * Adds the authentication scheme and token as an Authorization header to the given HTTP request builder.
	 *
	 * @param builder HTTP request builder to which the Authorization header is added
	 */
	@Override
	public void applyTo( final Builder builder ) {
		builder.setHeader( "Authorization", authenticationScheme + " " + token );
	}

	/**
	 * Adds the authentication scheme and token as an Authorization header to the given HTTP headers.
	 *
	 * @param headers HTTP headers to which the Authorization header is added
	 */
	@Override
	public void applyTo( final Map<String, String> headers ) {
		headers.put( "Authorization", authenticationScheme + " " + token );
	}

	/**
	 * Adds the authentication scheme and token as an Authorization header to the given HTTP headers.
	 *
	 * @param b QueryExecutionHTTPBuilder to which the Authorization header is added
	 */
	@Override
	public void applyTo( final QueryExecutionHTTPBuilder b ) {
		b.httpHeader( "Authorization", authenticationScheme + " " + token );
	}
}
