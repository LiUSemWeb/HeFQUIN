package se.liu.ida.hefquin.base.net.http;

import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.sun.net.httpserver.HttpServer;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class HttpClientProviderTest
{
	protected static HttpServer server;
	protected static String url;
	protected static final long requestDuration = 100;

	@BeforeClass
	public static void createExecService() throws IOException {
		server = setupHttpServerForTests();
		url = "http://localhost:" + server.getAddress().getPort() + "/test";
	}

	@Before
	public void resetProvider() {
		HttpClientProvider.reset();
	}

	@Test
	public void limitConcurrentRequestsToOneViaGlobal() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete sequentially, which means that the total time
		// should be greater than numberOfRequests * requestDuration
		final long end = System.currentTimeMillis();
		assertTrue( (numberOfRequests * requestDuration) < (end - start) );
	}

	@Test
	public void limitConcurrentRequestsToOne() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(20);
		HttpClientProvider.registerEndpointLimiter(url, 1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete sequentially, which
		// means that the total time should be greater than
		// numberOfRequests * delay
		final long end = System.currentTimeMillis();
		assertTrue( (numberOfRequests * requestDuration) < (end - start) );
	}

	@Test
	public void limitConcurrentRequestsToOneByHierarchicalEndpointAddress() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, 1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "/test" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete sequentially, which means that the total time
		// should be greater than numberOfRequests * requestDuration
		final long end = System.currentTimeMillis();
		assertTrue( (numberOfRequests * requestDuration) < (end - start) );
	}

		@Test
	public void noLimitOnConcurrentRequestsByHierarchicalEndpointAddress() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, 1);
		for ( int i = 0; i < numberOfRequests; i++ ) {
			HttpClientProvider.registerEndpointLimiter(url + "/test" + i, 1);
		}

		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "/test" + i + "/path");
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();


		// Executions should complete asynchronously. We add 1 sec to the time to avoid
		// flaky tests.
		final long end = System.currentTimeMillis();
		assertTrue( requestDuration + 1000 > (end - start) );
	}

	@Test
	public void limitConcurrentRequestsToTwo() throws IOException {
		final int numberOfRequests = 50;
		HttpClientProvider.registerEndpointLimiter(url, 2);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Two requests should be executed in parallel. The total time should be between
		// requestDuration and (numberOfRequests / 2) * requestDuration. We add 1 sec to
		// the time to avoid flaky tests.
		final long end = System.currentTimeMillis();
		assertTrue( requestDuration + 1000 < (end - start) );
		assertTrue( (numberOfRequests / 2) * requestDuration + 1000 > (end - start) );
	}

	@Test
	public void noLimitOnConcurrentRequestsViaGlobal() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously. We add 1 sec to the time to avoid
		// flaky tests.
		final long end = System.currentTimeMillis();
		assertTrue( requestDuration + 1000 > (end - start) );
	}

	@Test
	public void noLimitOnConcurrentRequests() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously. We add 1 sec to the time to avoid
		// flaky tests.
		final long end = System.currentTimeMillis();
		assertTrue( requestDuration + 1000 > (end - start) );
	}

	@Test
	public void endpointLimiterOverridesDefaultLimit() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(1);
		HttpClientProvider.registerEndpointLimiter(url, numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for ( int i = 0; i < numberOfRequests; i++ ) {
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously. We add 1 sec to the time to avoid
		// flaky tests.
		final long end = System.currentTimeMillis();
		assertTrue( requestDuration + 1000 > (end - start) );
	}

	protected static HttpServer setupHttpServerForTests() throws IOException {
		final HttpServer server = HttpServer.create( new InetSocketAddress(0), 0 );
		server.createContext( "/test", exchange -> {
			try {
				Thread.sleep(requestDuration); // artificial server delay
				final byte[] response = "ok".getBytes();
				exchange.sendResponseHeaders( 200, response.length );
				try ( OutputStream os = exchange.getResponseBody() ) {
					os.write(response);
				}
			} catch ( InterruptedException e ) {
				Thread.currentThread().interrupt();
				exchange.sendResponseHeaders( 500, -1 );
			} finally {
				exchange.close();
			}
		} );

		final ExecutorService serverExecutor = Executors.newCachedThreadPool();
		server.setExecutor(serverExecutor);
		server.start();
		return server;
	}
}
