package se.liu.ida.hefquin.engine.federation.access.impl;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

import se.liu.ida.hefquin.engine.federation.BRTPFServer;
import se.liu.ida.hefquin.engine.federation.Neo4jServer;
import se.liu.ida.hefquin.engine.federation.SPARQLEndpoint;
import se.liu.ida.hefquin.engine.federation.TPFServer;
import se.liu.ida.hefquin.engine.federation.access.*;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.BRTPFRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.Neo4jRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.SPARQLRequestProcessor;
import se.liu.ida.hefquin.engine.federation.access.impl.reqproc.TPFRequestProcessor;

public class AsyncFederationAccessManagerImpl extends FederationAccessManagerBase2
{
	public static int DEFAULT_THREAD_POOL_SIZE = 10;

	protected final ExecutorService threadPool;

	// stats
	protected long issuedSPARQLRequests  = 0L;
	protected long issuedTPFRequests     = 0L;
	protected long issuedBRTPFRequests   = 0L;
	protected long issuedNeo4jRequests   = 0L;
	protected AtomicLong completedSPARQLRequests  = new AtomicLong(0L);
	protected AtomicLong completedTPFRequests     = new AtomicLong(0L);
	protected AtomicLong completedBRTPFRequests   = new AtomicLong(0L);
	protected AtomicLong completedNeo4jRequests   = new AtomicLong(0L);

	public AsyncFederationAccessManagerImpl(
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		this(DEFAULT_THREAD_POOL_SIZE, reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);
	}

	public AsyncFederationAccessManagerImpl(
			final int threadPoolSize,
			final SPARQLRequestProcessor reqProcSPARQL,
			final TPFRequestProcessor reqProcTPF,
			final BRTPFRequestProcessor reqProcBRTPF,
			final Neo4jRequestProcessor reqProcNeo4j) {
		super(reqProcSPARQL, reqProcTPF, reqProcBRTPF, reqProcNeo4j);

		threadPool = Executors.newFixedThreadPool(threadPoolSize);
	}

	@Override
	public CompletableFuture<SolMapsResponse> issueRequest( final SPARQLRequest req, final SPARQLEndpoint fm )
	{
		issuedSPARQLRequests++;
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final TPFServer fm )
	{
		issuedTPFRequests++;
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final TPFRequest req, final BRTPFServer fm )
	{
		issuedTPFRequests++;
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<TPFResponse> issueRequest( final BRTPFRequest req, final BRTPFServer fm )
	{
		issuedBRTPFRequests++;
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	public CompletableFuture<RecordsResponse> issueRequest(final Neo4jRequest req, final Neo4jServer fm )
	{
		issuedNeo4jRequests++;
		return CompletableFuture.supplyAsync( createSupplier(req,fm), threadPool );
	}

	@Override
	protected FederationAccessStatsImpl _getStats() {
		return new FederationAccessStatsImpl( issuedSPARQLRequests,
		                                      issuedTPFRequests,
		                                      issuedBRTPFRequests,
		                                      issuedNeo4jRequests,
		                                      completedSPARQLRequests.get(),
		                                      completedTPFRequests.get(),
		                                      completedBRTPFRequests.get(),
		                                      completedNeo4jRequests.get() );
	}

	@Override
	public void resetStats() {
		issuedSPARQLRequests  = 0L;
		issuedTPFRequests     = 0L;
		issuedBRTPFRequests   = 0L;
		issuedNeo4jRequests   = 0L;

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
				completedNeo4jRequests.incrementAndGet();
				return resp;
			}
		};
	}

}
