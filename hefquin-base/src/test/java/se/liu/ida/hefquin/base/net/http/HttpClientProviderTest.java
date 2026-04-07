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
	protected static final long REQUEST_DURATION = 50;
	/**
	 * Extra time buffer added to timing assertions to reduce flakiness caused by
	 * thread scheduling, GC pauses, OS variability etc.
	 */
	protected static final long TIMING_TOLERANCE = 1000;

	protected enum TimingCheck {
		TOO_FAST, // checks against minExpected
		TOO_SLOW // checks against maxExpected
	}

	@BeforeClass
	public static void createExecService() throws IOException {
		server = setupHttpServerForTests();
		url = "http://localhost:" + server.getAddress().getPort() + "/";
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
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete sequentially
		long minExpected = numberOfRequests * REQUEST_DURATION;
		long maxExpected = numberOfRequests * REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_FAST, minExpected, duration), minExpected < duration );
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void limitConcurrentRequestsToOne() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(20);
		HttpClientProvider.registerEndpointLimiter(url, 1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete sequentially
		long minExpected = numberOfRequests * REQUEST_DURATION;
		long maxExpected = numberOfRequests * REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_FAST, minExpected, duration), minExpected < duration );
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void limitConcurrentRequestsToOneByHierarchicalEndpointAddress() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, 1);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete sequentially
		long minExpected = numberOfRequests * REQUEST_DURATION;
		long maxExpected = numberOfRequests * REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_FAST, minExpected, duration), minExpected < duration );
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void noLimitOnConcurrentRequestsByHierarchicalEndpointAddress() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, 1);
		for ( int i = 0; i < numberOfRequests; i++ ) {
			HttpClientProvider.registerEndpointLimiter(url + i, 1);
		}

		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously
		long maxExpected = REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void limitConcurrentRequestsToTwo() throws IOException {
		final int numberOfRequests = 50;
		HttpClientProvider.registerEndpointLimiter(url, 2);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Two requests should be executed in parallel
		long minExpected = (numberOfRequests / 2) * REQUEST_DURATION;
		long maxExpected = (numberOfRequests / 2) * REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_FAST, minExpected, duration), minExpected < duration );
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void noLimitOnConcurrentRequestsViaGlobal() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously
		long maxExpected = REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void noLimitOnConcurrentRequests() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.registerEndpointLimiter(url, numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously
		long maxExpected = REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	@Test
	public void endpointLimiterOverridesDefaultLimit() throws IOException {
		final int numberOfRequests = 20;
		HttpClientProvider.setDefaultMaxParallelRequests(1);
		HttpClientProvider.registerEndpointLimiter(url, numberOfRequests);
		final long start = System.currentTimeMillis();
		final HttpClient client = HttpClientProvider.client();
		final CompletableFuture<?>[] futures = sendAsyncRequests(client, url, numberOfRequests);

		// Wait for all futures to complete
		CompletableFuture.allOf(futures).join();

		// Executions should complete asynchronously
		long maxExpected = REQUEST_DURATION + TIMING_TOLERANCE;
		final long duration = System.currentTimeMillis() - start;
		assertTrue( formatTimingMsg(TimingCheck.TOO_SLOW, maxExpected, duration), maxExpected > duration );
	}

	// ------------ helper code ------------

	protected static CompletableFuture<?>[] sendAsyncRequests( final HttpClient client,
	                                                           final String baseUrl,
	                                                           final int numRequests ) {
		final CompletableFuture<?>[] futures = new CompletableFuture[numRequests];
		for ( int i = 0; i < numRequests; i++ ) {
			final URI uri = URI.create( baseUrl + i + "?q=" + i );
			final HttpRequest request = HttpRequest.newBuilder().uri(uri).GET().build();
			futures[i] = client.sendAsync( request, HttpResponse.BodyHandlers.ofString() );
		}
		return futures;
	}

	protected static String formatTimingMsg( final TimingCheck checkType,
	                                         final long expected,
	                                         final long duration ) {
		final String bound = (checkType == TimingCheck.TOO_FAST) ? "minExpected" : "maxExpected";
		return String.format( "Execution timing violation (%s):\n" +
		                      "  %s\t\t= %d ms\n" +
		                      "  actual duration\t= %d ms\n" +
		                      "  REQUEST_DURATION\t= %d ms\n" +
		                      "  TIMING_TOLERANCE\t= %d ms",
		                      checkType, bound, expected, duration, REQUEST_DURATION, TIMING_TOLERANCE );
	}

	protected static HttpServer setupHttpServerForTests() throws IOException {
		final HttpServer server = HttpServer.create( new InetSocketAddress(0), 0 );
		server.createContext( "/", exchange -> {
			try {
				Thread.sleep(REQUEST_DURATION); // artificial server delay
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

		final ExecutorService serverExecutor = Executors.newFixedThreadPool(50);
		server.setExecutor(serverExecutor);
		server.start();
		return server;
	}
}
