package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessorImpl;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessorImpl;

public class AsyncFederationAccessManagerImpl extends FederationAccessManagerBase2
{
	protected final ExecutorService threadPool;

	// stats
	protected AtomicLong issuedSPARQLRequests  = new AtomicLong(0L);
	protected AtomicLong issuedTPFRequests     = new AtomicLong(0L);
	protected AtomicLong issuedBRTPFRequests   = new AtomicLong(0L);
	protected AtomicLong issuedNeo4jRequests   = new AtomicLong(0L);
	protected AtomicLong completedSPARQLRequests  = new AtomicLong(0L);
	protected AtomicLong completedTPFRequests     = new AtomicLong(0L);
	protected AtomicLong completedBRTPFRequests   = new AtomicLong(0L);
	protected AtomicLong completedNeo4jRequests   = new AtomicLong(0L);

	public AsyncFederationAccessManagerImpl(
			final ExecutorService execService,
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		super(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);

		assert execService != null;
		threadPool = execService;
	}

	/**
	 * Creates an {@link AsyncFederationAccessManagerImpl} with a default configuration.
	 */
	public AsyncFederationAccessManagerImpl( final ExecutorService execService ) {
		this( execService,
		      new SPARQLRequestProcessorImpl(),
		      new TPFRequestProcessorImpl(),
		      new BRTPFRequestProcessorImpl(),
		      new Neo4jRequestProcessorImpl() );
	}

	@Override
	public CompletableFuture<SolMapsResponse> issueRequest( final SPARQLRequest req, final SPARQLEndpoint fm )
	{
		issuedSPARQLRequests.incrementAndGet();
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm )
	{
		issuedTPFRequests.incrementAndGet();
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm )
	{
		issuedTPFRequests.incrementAndGet();
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm )
	{
		issuedBRTPFRequests.incrementAndGet();
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<RecordsResponse> issueRequest(final Neo4jRequest req, final Neo4jServer fm )
	{
		issuedNeo4jRequests.incrementAndGet();
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	protected FederationAccessStatsImpl _getStats() {
		return new FederationAccessStatsImpl( issuedSPARQLRequests.get(),
		                                      issuedTPFRequests.get(),
		                                      issuedBRTPFRequests.get(),
		                                      issuedNeo4jRequests.get(),
		                                      completedSPARQLRequests.get(),
		                                      completedTPFRequests.get(),
		                                      completedBRTPFRequests.get(),
		                                      completedNeo4jRequests.get() );
	}

	@Override
	protected void _resetStats() {
		issuedSPARQLRequests.set(0L);
		issuedTPFRequests.set(0L);
		issuedBRTPFRequests.set(0L);
		issuedNeo4jRequests.set(0L);

		completedSPARQLRequests.set(0L);
		completedTPFRequests.set(0L);
		completedBRTPFRequests.set(0L);
		completedNeo4jRequests.set(0L);
	}


	protected Supplier<SolMapsResponse> createSupplier( final SPARQLRequest req,
	                                                    final SPARQLEndpoint fm )
	{
		return new Supplier<SolMapsResponse>() {
			@Override public SolMapsResponse get() {
				final SolMapsResponse resp;
				try {
					resp = reqProcSPARQL.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}

				if ( resp == null ) {
					throw new RuntimeException("reqProcSPARQL returned null");
				}

				completedSPARQLRequests.incrementAndGet();
				return resp;
			}
		};
	}

	protected Supplier<TPFResponse> createSupplier( final TPFRequest req,
	                                                final TPFServer fm )
	{
		return new Supplier<TPFResponse>() {
			@Override public TPFResponse get() {
				final TPFResponse resp;
				try {
					resp = reqProcTPF.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}

				if ( resp == null ) {
					throw new RuntimeException("reqProcTPF returned null");
				}

				completedTPFRequests.incrementAndGet();
				return resp;
			}
		};
	}

	protected Supplier<TPFResponse> createSupplier( final TPFRequest req,
	                                                final BRTPFServer fm )
	{
		return new Supplier<TPFResponse>() {
			@Override public TPFResponse get() {
				final TPFResponse resp;
				try {
					resp = reqProcTPF.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}

				if ( resp == null ) {
					throw new RuntimeException("reqProcTPF returned null");
				}

				completedTPFRequests.incrementAndGet();
				return resp;
			}
		};
	}

	protected Supplier<TPFResponse> createSupplier( final BRTPFRequest req,
	                                                final BRTPFServer fm )
	{
		return new Supplier<TPFResponse>() {
			@Override public TPFResponse get() {
				final TPFResponse resp;
				try {
					resp = reqProcBRTPF.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}

				if ( resp == null ) {
					throw new RuntimeException("reqProcBRTPF returned null");
				}

				completedBRTPFRequests.incrementAndGet();
				return resp;
			}
		};
	}

	protected Supplier<RecordsResponse> createSupplier( final Neo4jRequest req,
	                                                   final Neo4jServer fm )
	{
		return new Supplier<RecordsResponse>() {
			@Override public RecordsResponse get() {
				final RecordsResponse resp;
				try {
					resp = reqProcNeo4j.performRequest(req, fm);
				} catch ( final FederationAccessException e ) {
					throw new RuntimeException("Performing a request caused an exception.", e);
				}

				if ( resp == null ) {
					throw new RuntimeException("reqProcNeo4j returned null");
				}

				completedNeo4jRequests.incrementAndGet();
				return resp;
			}
		};
	}

	/**
	 * Shuts down all thread pools associated with this federation access manager.
	 */
	@Override
	public void shutdown() {
		threadPool.shutdown();
		try {
			if ( ! threadPool.awaitTermination(500L, TimeUnit.MILLISECONDS) ) {
				threadPool.shutdownNow();
			}
		} catch ( InterruptedException ex ) {
			Thread.currentThread().interrupt();
			threadPool.shutdownNow();
		}
	}
}
