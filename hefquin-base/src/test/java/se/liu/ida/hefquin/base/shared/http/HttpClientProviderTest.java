package se.liu.ida.hefquin.base.shared.http;

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

public class HttpClientProviderTest {

	protected static HttpServer server;
	protected static String url;
	protected static final long DELAY = 100;

	@BeforeClass
	public static void createExecService() throws IOException {
		server = setupHttpServerForTests();
		url = "http://localhost:" + server.getAddress().getPort() + "/test";
	}

	@Before
	public void resetProvider() {
		HttpClientProvider.resetForTests();
	}

	@Test
	public void limitConcurrentRequestsToOneViaGlobal() throws IOException{
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for(int i=0; i < numberOfRequests; i++){
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
		assertTrue( (numberOfRequests * DELAY) < (end - start) );
	}

	@Test
	public void limitConcurrentRequestsToOne() throws IOException{
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, 1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for(int i=0; i < numberOfRequests; i++){
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
		assertTrue( (numberOfRequests * DELAY) < (end - start) );
	}

	@Test
	public void limitConcurrentRequestsToTwo() throws IOException{
		final int numberOfRequests = 50;
		HttpClientProvider.registerEndpointLimiter(url, 2);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for(int i=0; i < numberOfRequests; i++){
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Two requests should executed in two parallel. The total time should be
		// approximately numberOfRequests * delay / 2
		final long end = System.currentTimeMillis();
		System.err.println(numberOfRequests * DELAY);
		System.err.println(end - start);
		assertTrue( ((numberOfRequests * DELAY) / 2) + 1000 > (end - start) );
	}

	@Test
	public void noLimitOnConcurrentRequestsViaGlobal() throws IOException{
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for(int i=0; i < numberOfRequests; i++){
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete in a fraction of the time.
		// To avoid flaky tests, we assume that the time is 1/10
		// of the minimum sequential time.
		final long end = System.currentTimeMillis();
		assertTrue( DELAY + 1000 > (end - start) );
	}

	@Test
	public void noLimitOnConcurrentRequests() throws IOException{
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for(int i=0; i < numberOfRequests; i++){
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete in a fraction of the time.
		// To avoid flaky tests, we assume that the time is 1/10
		// of the minimum sequential time.
		final long end = System.currentTimeMillis();
		assertTrue( DELAY + 1000 > (end - start) );
	}

	@Test
	public void endpointLimiterOverridesDefaultLimit() throws IOException{
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(1);
		HttpClientProvider.registerEndpointLimiter(url, numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = new CompletableFuture[numberOfRequests];

		for(int i=0; i < numberOfRequests; i++){
			final URI uri = URI.create( url + "?i=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete in a fraction of the time.
		// To avoid flaky tests, we assume that the time is 1/10
		// of the minimum sequential time.
		final long end = System.currentTimeMillis();
		assertTrue( DELAY + 1000 > (end - start) );
	}

	protected static HttpServer setupHttpServerForTests() throws IOException {
		final HttpServer server = HttpServer.create( new InetSocketAddress(0), 0 );
		server.createContext( "/test", exchange -> {
			try {
				Thread.sleep( DELAY ); // artificial server delay
				final byte[] response = "ok".getBytes();
				exchange.sendResponseHeaders( 200, response.length );
				try ( OutputStream os = exchange.getResponseBody() ) {
					os.write( response );
				}
			} catch ( InterruptedException e ) {
				Thread.currentThread().interrupt();
				exchange.sendResponseHeaders(500, -1);
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
