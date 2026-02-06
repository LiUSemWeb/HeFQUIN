package se.liu.ida.hefquin.base.shared.http;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Provides shared {@link java.net.http.HttpClient} instances.
 *
 * Each {@code HttpClient} instance manages its own internal connection pool.
 * A single client handles requests to many different hosts while reusing
 * connections where possible.
 *
 * The {@code connectTimeout} is a client-level configuration and cannot be
 * changed after a client is created. To support different connection-timeout
 * requirements, the provider returns a different {@code HttpClient} instance
 * for each timeout value. Each such client then manages its own independent set
 * of pooled connections for all routes it is used with.
 */
public class HttpClientProvider
{
	// No timeout
	protected static final long NO_TIMEOUT = -1;

	// Cache by connect timeout
	protected static final Map<Long, HttpClient> BY_CONNECT_TIMEOUT = new ConcurrentHashMap<>();

	public static HttpClient client() {
		return client(NO_TIMEOUT);
	}

	public static HttpClient client( final long connectTimeout ) {
		// Normalize: <= 0 means "no limit"
		final long effectiveTimeout = connectTimeout <= 0 ? NO_TIMEOUT : connectTimeout;

		return BY_CONNECT_TIMEOUT.computeIfAbsent( effectiveTimeout, t -> {
			final HttpClient.Builder builder = HttpClient.newBuilder()
				.followRedirects( HttpClient.Redirect.ALWAYS )
				.version( HttpClient.Version.HTTP_2 );

			if ( t != NO_TIMEOUT ) {
				builder.connectTimeout( Duration.ofMillis(t) );
			}

			return builder.build();
		} );
	}
}
