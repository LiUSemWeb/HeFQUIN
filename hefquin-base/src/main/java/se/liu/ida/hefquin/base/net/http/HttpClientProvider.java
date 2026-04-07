package se.liu.ida.hefquin.base.net.http;

import java.io.IOException;
import java.net.Authenticator;
import java.net.CookieHandler;
import java.net.ProxySelector;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Semaphore;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;

/**
 * Provides shared {@link java.net.http.HttpClient} instances.
 *
 * <p>
 * Each {@code HttpClient} instance manages its own internal connection pool. A
 * single client handles requests to many different hosts while reusing
 * connections where possible.
 * </p>
 *
 * <p>
 * The {@code connectTimeout} is a client-level configuration and cannot be
 * changed after a client has been created. To support different
 * connection-timeout values, this provider caches one client instance per
 * timeout. Each such client manages its own independent set of pooled
 * connections for all routes it is used with.
 * </p>
 *
 * <p>
 * Returned {@link HttpClient} instances are backed by an internal
 * {@link LimitedHttpClient} wrapper to enforce concurrency limits using
 * {@link Semaphore}s. Applicable concurrency limits are applied based on
 * hierarchical lookup by trimming path segments from the request URI.
 * </p>
 */
public class HttpClientProvider
{
	// No timeout
	private static final long NO_TIMEOUT = -1;

	// Default max number of parallel requests per endpoint address
	private static final int DEFAULT_MAX_PARALLEL_REQUESTS = 10;

	// Mutable default (can be overridden)
	private static int defaultMaxParallelRequests = DEFAULT_MAX_PARALLEL_REQUESTS;

	/** One limiter per endpoint address. The limiter for an endpoint address is shared across all clients. */
	protected static final Map<String, Semaphore> LIMITERS_BY_ENDPOINT_ADDRESS = new ConcurrentHashMap<>();

	// Cache clients by connect timeout
	protected static final Map<Long, HttpClient> CLIENT_BY_CONNECT_TIMEOUT = new ConcurrentHashMap<>();

	/**
	 * Returns a shared client with no configured connect timeout.
	 *
	 * @return a shared client instance
	 */
	public static HttpClient client() {
		return client(NO_TIMEOUT);
	}

	/**
	 * Returns a shared client for a given connect timeout.
	 *
	 * A separate client instance is cached for each effective timeout value, where
	 * non-positive values are treated as "no connect timeout".
	 *
	 * @param connectTimeout connect timeout in milliseconds, or non-positive value
	 *                       for no timeout
	 * @return shared client instance for the given timeout configuration
	 */
	public static HttpClient client( final long connectTimeout ) {
		final long effectiveTimeout = connectTimeout <= 0 ? NO_TIMEOUT : connectTimeout;

		return CLIENT_BY_CONNECT_TIMEOUT.computeIfAbsent( effectiveTimeout, t -> {
			final HttpClient.Builder builder = HttpClient.newBuilder()
				.followRedirects( HttpClient.Redirect.ALWAYS )
				.version( HttpClient.Version.HTTP_2 );

			if ( t != NO_TIMEOUT ) {
				builder.connectTimeout( Duration.ofMillis(t) );
			}

			final HttpClient delegate = builder.build();
			return new LimitedHttpClient(delegate);
		} );
	}

	/**
	 * Sets the default maximum number of concurrent requests per endpoint address.
	 *
	 * This value is used when creating new endpoint limiters via
	 * {@link #getOrCreateEndpointLimiter(String)}.
	 *
	 * @param maxParallelRequests maximum number of concurrent requests per
	 *                            endpoint address
	 * @throws IllegalArgumentException if {@code maxParallelRequests} is non-positive
	 */
	public static void setDefaultMaxParallelRequests( final int maxParallelRequests ) {
		if ( maxParallelRequests <= 0 ) {
			throw new IllegalArgumentException("maxParallelRequests must be greater than zero");
		}
		defaultMaxParallelRequests = maxParallelRequests;
	}

	/**
	 * Registers a concurrency limiter for the given endpoint address.
	 *
	 * If a limiter is already registered for the endpoint address, it is replaced.
	 *
	 * @param endpointAddress     endpoint address
	 * @param maxParallelRequests maximum number of concurrent requests allowed for
	 *                            the endpoint address
	 * @throws IllegalArgumentException if maxParallelRequests is non-positive.
	 */
	public static void registerEndpointLimiter( final String endpointAddress, final int maxParallelRequests ) {
		if ( maxParallelRequests <= 0 ) {
			throw new IllegalArgumentException("maxParallelRequests must be greater than zero");
		}
		LIMITERS_BY_ENDPOINT_ADDRESS.put( endpointAddress, new Semaphore(maxParallelRequests, true) );
	}

	/**
	 * Resolves the concurrency limiter for the given URI.
	 *
	 * <p>
	 * If the URI has no path, or only the root path {@code "/"}, the limiter for
	 * the origin (scheme + authority) is returned, creating a default limiter if
	 * none exists. Otherwise, the method progressively removes path segments while
	 * checking for a registered limiter, falling back to the origin if none is
	 * found. Trailing slashes are removed before matching endpoint addresses.
	 * </p>
	 *
	 * <p>
	 * For example, for {@code http://example.org/part/of/url?q=x}:
	 * </p>
	 * <ul>
	 * <li>{@code http://example.org/part/of/url}</li>
	 * <li>{@code http://example.org/part/of}</li>
	 * <li>{@code http://example.org/part}</li>
	 * <li>{@code http://example.org}</li>
	 * </ul>
	 *
	 * <p>
	 * If no limiter is registered for any of the above, a default limiter is
	 * created for the origin and returned.
	 * </p>
	 *
	 * @param uri the request URI
	 * @return the most specific matching limiter, or a default limiter for the
	 *         origin if none is found
	 */
	private static Semaphore resolveLimiter( final URI uri ) {
		final String origin = uri.getScheme() + "://" + uri.getAuthority();

		if ( uri.getPath() == null || uri.getPath().isEmpty() || uri.getPath().equals( "/" ) ) {
			return LIMITERS_BY_ENDPOINT_ADDRESS.computeIfAbsent( origin, k -> newDefaultLimiter() );
		}

		String candidate = origin + uri.getPath();
		// Remove trailing slash
		if ( candidate.endsWith("/") ) {
			candidate = candidate.substring( 0, candidate.length() - 1 );
		}

		while ( candidate.length() >= origin.length() ) {
			final Semaphore limiter = LIMITERS_BY_ENDPOINT_ADDRESS.get(candidate);
			if ( limiter != null ) {
				return limiter;
			}

			final int lastSlash = candidate.lastIndexOf('/');
			if ( lastSlash < origin.length() ) {
				break;
			}
			candidate = candidate.substring(0, lastSlash);
		}

		return LIMITERS_BY_ENDPOINT_ADDRESS.computeIfAbsent( origin, k -> newDefaultLimiter() );
	}

	/**
	 * Creates a new default concurrency limiter.
	 *
	 * <p>
	 * The returned limiter enforces the current {@link #defaultMaxParallelRequests}
	 * and uses a fair (FIFO) scheduling policy.
	 * </p>
	 *
	 * @return a new {@link Semaphore} configured with the default concurrency limit
	 */
	private static Semaphore newDefaultLimiter() {
		return new Semaphore( defaultMaxParallelRequests, true );
	}

	/**
	 * Resets provider state to its default configuration.
	 *
	 * <p>
	 * This clears all cached clients and registered endpoint address limiters, and
	 * restores the default maximum number of parallel requests.
	 * </p>
	 *
	 * <p>
	 * This method is primarily intended for test isolation.
	 * </p>
	 */
	public static void reset() {
		CLIENT_BY_CONNECT_TIMEOUT.clear();
		LIMITERS_BY_ENDPOINT_ADDRESS.clear();
		defaultMaxParallelRequests = DEFAULT_MAX_PARALLEL_REQUESTS;
	}

	static final class LimitedHttpClient extends HttpClient
	{
		private final HttpClient delegate;

		private LimitedHttpClient( final HttpClient delegate ) {
			this.delegate = delegate;
		}

		@Override
		public <T> HttpResponse<T> send( final HttpRequest request,
		                                 final HttpResponse.BodyHandler<T> responseBodyHandler )
				throws IOException, InterruptedException {
			final Semaphore limiter = resolveLimiter( request.uri() );
			limiter.acquire();
			try {
				return delegate.send(request, responseBodyHandler);
			} finally {
				limiter.release();
			}
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync( final HttpRequest request,
		                                                         final HttpResponse.BodyHandler<T> responseBodyHandler ) {
			final Semaphore limiter = resolveLimiter( request.uri() );

			try {
				limiter.acquire();
			} catch ( InterruptedException e ) {
				final CompletableFuture<HttpResponse<T>> failed = new CompletableFuture<>();
				failed.completeExceptionally(e);
				Thread.currentThread().interrupt();
				return failed;
			}

			return delegate.sendAsync(request, responseBodyHandler).whenComplete( (r, t) -> limiter.release() );
		}

		@Override
		public <T> CompletableFuture<HttpResponse<T>> sendAsync( final HttpRequest request,
		                                                         final HttpResponse.BodyHandler<T> responseBodyHandler,
		                                                         final HttpResponse.PushPromiseHandler<T> pushPromiseHandler ) {
			final Semaphore limiter = resolveLimiter( request.uri() );
			try {
				limiter.acquire();
			} catch ( InterruptedException e ) {
				final CompletableFuture<HttpResponse<T>> failed = new CompletableFuture<>();
				failed.completeExceptionally(e);
				Thread.currentThread().interrupt();
				return failed;
			}

			return delegate.sendAsync(request, responseBodyHandler, pushPromiseHandler)
					.whenComplete( (r, t) -> limiter.release() );
		}

		// Delegate all other methods

		@Override
		public Optional<CookieHandler> cookieHandler() {
			return delegate.cookieHandler();
		}

		@Override
		public Optional<Duration> connectTimeout() {
			return delegate.connectTimeout();
		}

		@Override
		public Redirect followRedirects() {
			return delegate.followRedirects();
		}

		@Override
		public Optional<ProxySelector> proxy() {
			return delegate.proxy();
		}

		@Override
		public SSLContext sslContext() {
			return delegate.sslContext();
		}

		@Override
		public SSLParameters sslParameters() {
			return delegate.sslParameters();
		}

		@Override
		public Optional<Authenticator> authenticator() {
			return delegate.authenticator();
		}

		@Override
		public Version version() {
			return delegate.version();
		}

		@Override
		public Optional<Executor> executor() {
			return delegate.executor();
		}
	}
}
