package se.liu.ida.hefquin.federation.members;

import java.net.http.HttpRequest;
import java.util.Map;

/**
 * Provides authentication information for requests to a federation member.
 * <p>
 * The information is provided in a form that it can be applied directly
 * to an HTTP request builder or to a collection of HTTP headers.
 */
public interface AuthenticationInformation
{
	/**
	 * Applies the authentication information to the given HTTP request builder.
	 *
	 * @param builder HTTP request builder to which the authentication information
	 *                is applied
	 */
	void applyTo( HttpRequest.Builder builder );

	/**
	 * Applies the authentication information to the given HTTP headers.
	 *
	 * @param headers HTTP headers to which the authentication information is applied
	 */
	void applyTo( Map<String, String> headers );
}
