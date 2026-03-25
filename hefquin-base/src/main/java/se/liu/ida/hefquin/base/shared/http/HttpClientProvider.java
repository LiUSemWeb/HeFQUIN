package se.liu.ida.hefquin.base.shared.http;

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
 * Each {@code HttpClient} instance manages its own internal connection pool. A
 * single client handles requests to many different hosts while reusing
 * connections where possible.
 *
 * The {@code connectTimeout} is a client-level configuration and cannot be
 * changed after a client has been created. To support different
 * connection-timeout values, this provider caches one client instance per
 * timeout. Each such client manages its own independent set of pooled
 * connections for all routes it is used with.
 *
 * Returned clients are wrapped to enforce concurrency limits using
 * {@link Semaphore}s. Limits are applied per endpoint key, where the key is
 * derived from the request URI by {@link #toEndpointKey(URI)} unless explicitly
 * configured otherwise.
 */
public class HttpClientProvider
{
	// No timeout
	private static final long NO_TIMEOUT = -1;

	// Default max number of parallel requests per endpoint
	private static final int DEFAULT_MAX_PARALLEL_REQUESTS = 10;

	/** One limiter per endpoint key, shared across all clients from this provider. */
	protected static final Map<String, Semaphore> limiterMap = new ConcurrentHashMap<>();

	// Cache clients by connect timeout
	protected static final Map<Long, HttpClient> BY_CONNECT_TIMEOUT = new ConcurrentHashMap<>();

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

		return BY_CONNECT_TIMEOUT.computeIfAbsent( effectiveTimeout, t -> {
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
	 * Registers a concurrency limiter for the given endpoint key.
	 *
	 * If a limiter is already registered for the key, it is replaced.
	 *
	 * @param key                 endpoint key
	 * @param maxParallelRequests maximum number of concurrent requests allowed for
	 *                            the endpoint
	 */
	public static void registerEndpointLimiter( final String key, final int maxParallelRequests ) {
		limiterMap.put( key, new Semaphore(maxParallelRequests, true) );
	}

	/**
	 * Returns the limiter registered for the given endpoint key, creating one with
	 * the default limit if none exists.
	 *
	 * @param key endpoint key
	 * @return limiter for the endpoint
	 */
	private static Semaphore getOrCreateEndpointLimiter( final String key ) {
		return limiterMap.computeIfAbsent( key, k -> new Semaphore( DEFAULT_MAX_PARALLEL_REQUESTS, true ) );
	}

	/**
	 * Derives the endpoint key for the given URI.
	 *
	 * The endpoint key is the request URI without query and fragment components.
	 *
	 * @param uri request URI
	 * @return endpoint key used for concurrency limiting
	 */
	private static String toEndpointKey( final URI uri ) {
		return uri.resolve( uri.getPath() ).toString();
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
			final String endpointKey = toEndpointKey( request.uri() );
			final Semaphore limiter = getOrCreateEndpointLimiter(endpointKey);
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
			final String endpointKey = toEndpointKey( request.uri() );
			final Semaphore limiter = getOrCreateEndpointLimiter(endpointKey);

			try {
				limiter.acquire();
			} catch ( final InterruptedException e ) {
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
			final String endpointKey = toEndpointKey( request.uri() );
			final Semaphore limiter = getOrCreateEndpointLimiter(endpointKey);
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
