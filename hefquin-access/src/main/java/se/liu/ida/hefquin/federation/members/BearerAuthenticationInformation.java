package se.liu.ida.hefquin.federation.members;

import java.net.http.HttpRequest.Builder;
import java.util.Map;

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
}
